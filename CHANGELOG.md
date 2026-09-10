# Changelog

All notable changes to pingwin are documented in this file.

## [0.1.6] - 2026-09-10

### Security & Privacy

- Added redaction of sensitive data in diagnostic logs, including server addresses, domains, UUIDs, tokens and authorization data.
- Added verification of downloaded update APKs before installation.
- Excluded sensitive application data from Android backup and restore.
- Added optional external GeoIP server-country detection.
- External GeoIP requests are disabled by default and require explicit user opt-in.
- Server addresses remain masked in the interface to reduce accidental disclosure.

### Changed

- Improved update version comparison, including prerelease versions, multi-digit versions, `v` prefixes and build metadata.
- Improved Wi-Fi SSID detection and retry behavior for VPN automation.
- Added consistent server port validation across supported protocols.
- Updated the About screen for the current multi-protocol architecture.
- Added GitHub and Telegram support links to the About screen.
- Updated Russian and English interface text.
- Updated application version to 0.1.6.

### Fixed

- Improved handling of invalid server ports.
- Improved reliability of Wi-Fi based automation.

---

## [0.1.5] - 2026-09-06

### Added

- Added universal multi-protocol connection support.
- Added additional VLESS transports beyond TCP + REALITY.
- Added Hysteria2 support.
- Added Trojan support.
- Added VMess support.
- Added experimental TUIC configuration support.
- Added experimental Shadowsocks configuration support.
- Added QR code scanning and clipboard import for supported connections.
- Added universal libbox-based latency testing.

### Changed

- Improved connection management and protocol detection.
- Improved routing reliability for multi-protocol connections.
- Masked server addresses in the interface to reduce accidental disclosure in screenshots.
- Updated documentation for the multi-protocol architecture.

### Notes

- Hysteria2 with a self-signed TLS certificate requires `insecure=1` in the connection link.
- TUIC and Shadowsocks support was experimental in 0.1.5.
- Network compatibility may vary by ISP, mobile operator, country and local filtering.

