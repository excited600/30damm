import { ExpoConfig, ConfigContext } from "expo/config";

export default ({ config }: ConfigContext): ExpoConfig => ({
  ...config,
  name: "서티댐",
  slug: "30damm-mobile",
  version: "1.0.0",
  orientation: "portrait",
  icon: "./assets/icon.png",
  scheme: "app30damm",
  userInterfaceStyle: "automatic",
  splash: {
    backgroundColor: "#161616",
  },
  ios: {
    supportsTablet: true,
    bundleIdentifier: "com.samosao.30damm",
    buildNumber: "1",
    infoPlist: {
      NSAppTransportSecurity: {
        NSAllowsArbitraryLoads: process.env.APP_ENV !== "production",
        NSAllowsLocalNetworking: true,
      },
    },
  },
  android: {
    adaptiveIcon: {
      backgroundColor: "#121212",
      foregroundImage: "./assets/android-icon-foreground.png",
      backgroundImage: "./assets/android-icon-background.png",
      monochromeImage: "./assets/android-icon-monochrome.png",
    },
    package: "com.samosao.thirtydamm",
  },
  web: {
    bundler: "metro",
    output: "static",
    favicon: "./assets/favicon.png",
  },
  plugins: [
    "expo-router",
    "expo-font",
    [
      "expo-splash-screen",
      {
        image: "./assets/splash-icon.png",
        imageWidth: 250,
        resizeMode: "contain",
        backgroundColor: "#161616",
      },
    ],
  ],
  experiments: {
    typedRoutes: true,
  },
  extra: {
    eas: {
      projectId: "32e4d142-0909-4830-ad77-1a6499099327"
    },
    apiUrl: process.env.API_URL ?? "http://localhost:8080",
    appEnv: process.env.APP_ENV ?? "local",
  },
});
