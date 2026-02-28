import { TextStyle } from "react-native";

export const typography = {
  display: {
    fontSize: 40,
    lineHeight: 48,
    fontWeight: "700",
  } as TextStyle,
  heading: {
    lg: {
      fontSize: 32,
      lineHeight: 40,
      fontWeight: "700",
    } as TextStyle,
    md: {
      fontSize: 28,
      lineHeight: 36,
      fontWeight: "600",
    } as TextStyle,
    sm: {
      fontSize: 24,
      lineHeight: 32,
      fontWeight: "600",
    } as TextStyle,
  },
  body: {
    lg: {
      fontSize: 16,
      lineHeight: 24,
      fontWeight: "400",
    } as TextStyle,
    md: {
      fontSize: 14,
      lineHeight: 20,
      fontWeight: "400",
    } as TextStyle,
    sm: {
      fontSize: 12,
      lineHeight: 16,
      fontWeight: "400",
    } as TextStyle,
  },
  label: {
    lg: {
      fontSize: 16,
      lineHeight: 24,
      fontWeight: "500",
    } as TextStyle,
    md: {
      fontSize: 14,
      lineHeight: 20,
      fontWeight: "500",
    } as TextStyle,
    sm: {
      fontSize: 12,
      lineHeight: 16,
      fontWeight: "400",
    } as TextStyle,
  },
  button: {
    lg: {
      fontSize: 16,
      lineHeight: 24,
      fontWeight: "600",
    } as TextStyle,
    md: {
      fontSize: 14,
      lineHeight: 20,
      fontWeight: "600",
    } as TextStyle,
  },
  caption: {
    fontSize: 11,
    lineHeight: 16,
    fontWeight: "400",
  } as TextStyle,
} as const;
