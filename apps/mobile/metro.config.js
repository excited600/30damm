const { getDefaultConfig } = require("expo/metro-config");

const config = getDefaultConfig(__dirname);

// expo 버전 낮추면서 run ios가 안되는 문제 해결 과정에서 추가됐는데, 꼭 필요하진 않은것같지만 두는게 낫다고해서 둔다.
config.resolver.unstable_enablePackageExports = true;

module.exports = config;
