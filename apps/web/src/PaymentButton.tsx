import {useEffect} from 'react';

// 포트원 V2 SDK 타입 정의
interface PaymentResponse {
    code?: string;
    message?: string;
    paymentId?: string;
    transactionType?: string;
    paymentToken: string;
    txId: string;
}

interface PaymentRequest {
    storeId: string;
    channelKey: string;
    paymentId: string;
    orderName: string;
    totalAmount: number;
    currency: string;
    payMethod: string;
    customer?: {
        fullName?: string;
        phoneNumber?: string;
        email?: string;
    };
    redirectUrl?: string;
}

interface PortOne {
    requestPayment: (params: PaymentRequest) => Promise<PaymentResponse>;
}

declare global {
    interface Window {
        PortOne: PortOne;
    }
}

// API 호출 함수들
const preparePayment = async (paymentId: string, paymentData: {

    productType: string;
    productUuid: string;
    productName: string;
    amount: number;
    buyerEmail: string;
    buyerName: string;
    buyerPhone: string;

}) => {
    const response = await fetch(`http://localhost:8080/payments/${paymentId}/prepare`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(paymentData),
    });

    if (!response.ok) {
        throw new Error('결제 준비 실패');
    }

    return response.json();
};

const verifyPayment = async (paymentId: string, verifyData: {

    paymentToken: string,
    txId: string,
    amount: number
}) => {
    const response = await fetch(`http://localhost:8080/payments/${paymentId}/verify`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(verifyData)
    });

    if (!response.ok) {
        throw new Error('결제 검증 실패');
    }

    return response.json();
};

function PaymentButton() {
    useEffect(() => {
        const script = document.createElement('script');
        script.src = 'https://cdn.portone.io/v2/browser-sdk.js';
        script.async = true;
        document.head.appendChild(script);

        return () => {
            document.head.removeChild(script);
        };
    }, []);

    const handlePayment = async () => {
        const {PortOne} = window;
        const paymentId = `GW8_payment_${Date.now()}`;
        const productType = "GATHERING"
        const productUuid = "62252cce-ecc4-468f-b422-4cdd0e5bac98";
        const productName = '상품명';
        const amount = 500;
        const buyerEmail = "wom2277@naver.com";
        const buyerName = "임근원";
        const buyerPhone = "010-1234-5678"
        try {
            // 1. 결제 준비 API 호출 (UI 렌더링 전)
            await preparePayment(paymentId,
                {
                    productType,
                    productUuid,
                    productName,
                    amount,
                    buyerEmail,
                    buyerName,
                    buyerPhone,
                });
            console.log('결제 준비 완료');

            // 2. 포트원 결제창 호출 (UI 렌더링)
            const response = await PortOne.requestPayment({
                storeId: 'store-c47dce95-6209-4f04-87f1-78914a40214a',
                channelKey: 'channel-key-8704e9d0-feb4-4cdd-96a6-34e8238a726f',
                paymentId,
                orderName: productName,
                totalAmount: amount,
                currency: 'KRW',
                payMethod: 'CARD',
                customer: {
                    fullName: buyerName,
                    phoneNumber: buyerPhone,
                }
            });

            console.log("response");
            console.log(response);
            //
            if (response.code) {
                // 에러 발생
                console.log('결제 실패', response.message);
            } else {
                // 3. 결제 성공 → 검증 API 호출
                const verifyResult = await verifyPayment(
                    response.paymentId!,
                    {
                        paymentToken: response.paymentToken,
                        txId: response.txId,
                        amount: amount
                    });
                console.log('결제 검증 완료', verifyResult);
            }
        } catch (error) {
            console.log('결제 오류', error);
        }
    };

    return <button onClick={handlePayment}>결제하기</button>;
}

export default PaymentButton;