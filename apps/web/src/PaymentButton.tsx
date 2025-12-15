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
    buyerUuid: string;
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

// SignUp API 호출
const signUp = async (userData: {
    email: string;
    nickname: string;
    age: number;
    gender: 'MALE' | 'FEMALE';
    introduction: string;
    password: string;
    phoneNumber: string;
    phoneAuthenticated: boolean;
}) => {
    const response = await fetch('http://localhost:8080/users/', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(userData),
    });

    if (!response.ok) {
        throw new Error('회원가입 실패');
    }

    return response.json();
};

// Gathering Open API 호출
const openGathering = async (gatheringData: {
    hostUuid: string;
    approveType: 'FIRST_IN' | 'APPROVAL';
    minCapacity: number;
    maxCapacity: number;
    genderRatioEnabled: boolean;
    minAge: number;
    maxAge: number;
    fee: number;
    discountEnabled: boolean;
    offline: boolean;
    place: string;
    category: string;
    subCategory: string;
    imageUrl: string;
    title: string;
    introduction: string;
    startDateTime: string;
    maxMaleCount?: number;
    maxFemaleCount?: number;
    duration?: number;
}) => {
    const response = await fetch('http://localhost:8080/gatherings', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(gatheringData),
    });

    if (!response.ok) {
        throw new Error('Gathering Open 실패');
    }

    return response.json();
};

// Gathering Join API 호출
const joinGathering = async (gatheringUuid: string, joinData: {
    userUuid: string;
    paymentId: string;
    paymentToken: string;
    txId: string;
    amount: number;
}) => {
    const response = await fetch(`http://localhost:8080/gatherings/${gatheringUuid}/join`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(joinData),
    });

    if (!response.ok) {
        throw new Error('Gathering Join 실패');
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
        const productName = '상품명';
        const amount = 500;
        const buyerEmail = "wom2277@naver.com";
        const buyerName = "임근원";
        const buyerPhone = "010-1234-5678"
        try {
            // host User 생성
            const hostResponse = await signUp({
                email: 'host@example.com',
                nickname: '호스트',
                age: 30,
                gender: 'MALE',
                introduction: '모임을 주최하는 호스트입니다.',
                password: 'password123',
                phoneNumber: '010-1111-1111',
                phoneAuthenticated: true,
            });
            console.log('Host 회원가입 완료:', hostResponse);
            const hostUuid = hostResponse.uuid;

            // guest User 생성
            const guestResponse = await signUp({
                email: buyerEmail,
                nickname: buyerName,
                age: 25,
                gender: 'FEMALE',
                introduction: '모임에 참여하는 게스트입니다.',
                password: 'password123',
                phoneNumber: buyerPhone,
                phoneAuthenticated: true,
            });
            console.log('Guest 회원가입 완료:', guestResponse);
            const guestUuid = guestResponse.uuid;

            // host User가 gathering open
            const gatheringResponse = await openGathering({
                hostUuid,
                approveType: 'FIRST_IN',
                minCapacity: 2,
                maxCapacity: 10,
                genderRatioEnabled: false,
                minAge: 20,
                maxAge: 40,
                fee: amount,
                discountEnabled: false,
                offline: true,
                place: '서울시 강남구',
                category: 'PARTY',
                subCategory: 'HOME_PARTY',
                imageUrl: 'https://example.com/image.jpg',
                title: productName,
                introduction: '모임 소개입니다.',
                startDateTime: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(), // 일주일 후
            });
            console.log('Gathering Open 완료:', gatheringResponse);
            const gatheringUuid = gatheringResponse.uuid;

            // 1. 결제 준비 API 호출 (UI 렌더링 전)
            await preparePayment(paymentId,
                {
                    productType,
                    productUuid: gatheringUuid,
                    productName,
                    amount,
                    buyerUuid: guestUuid,
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
            //
            if (response.code) {
                // 에러 발생
                console.log('결제 실패', response.message);
            } else {
                // guest 유저가 gathering 에 join
                await joinGathering(gatheringUuid, {
                    userUuid: guestUuid,
                    paymentId,
                    paymentToken: response.paymentToken,
                    txId: response.txId,
                    amount,
                });
                console.log('Gathering Join 완료');
            }
        } catch (error) {
            console.log('결제 오류', error);
        }
    };

    return <button onClick={handlePayment}>결제하기</button>;
}

export default PaymentButton;