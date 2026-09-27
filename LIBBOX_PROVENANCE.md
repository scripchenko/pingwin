# libbox provenance

## Current bundled artifact

File: `app/libs/libbox.aar`

SHA-256:

`3A706062E1B3E804CA7F7ABBDEF523A05C4AD7C125AB1496E2B0A52A62C360A5`

Size: 92,997,417 bytes.

Architectures bundled in the AAR:

- arm64-v8a
- armeabi-v7a
- x86
- x86_64

The current artifact entered its present form in pingwin commit `4d66fda` on 2026-09-06 and is tracked through Git LFS.

## Proven source state

The current bundled AAR has been reproduced byte-for-byte from the retained sing-box source repository.

Upstream repository:

`https://github.com/SagerNet/sing-box.git`

Upstream release:

`v1.13.12`

Upstream base commit:

`1086ab2563320e0da0c23b3a491d8dfa0939dff4`

Local source history:

1. `1086ab2563320e0da0c23b3a491d8dfa0939dff4` — upstream `v1.13.12`.
2. `2575adaffcdd4fcac419cb568268b6a73e151e4a` — `Use platform binding without default network strategy`.
3. One uncommitted source change was present at build time in `daemon/started_service.go`:

   `if len(g.Items) < 2 {`

   changed to:

   `if len(g.Items) < 1 {`

That one-line change was subsequently committed as:

`9dda45f135dfad38f1e698fee64dbed7ef7d08f1` — `fix: expose single-item outbound groups`

Because the AAR was built before that change was committed, its embedded version string reports:

`1.13.12-2575ada`

This is expected and does not mean the single-item outbound-group fix is absent.

## Reproduced build environment

The byte-identical reproduction was performed on 2026-09-27 using:

- Go: `go1.26.6`
- gomobile: `github.com/sagernet/gomobile v0.1.12`
- Android NDK: `30.0.15729638`
- target: Android
- build command:

  `go run ./cmd/internal/build_libbox -target android`

The sing-box build script warns that NDK `30.0.15729638` differs from its preferred NDK `28.0.13004108`, but NDK 30 is the toolchain that reproduces the bundled legacy artifact.

A reconstruction from commit `2575ada` plus the uncommitted `daemon/started_service.go` change, built with NDK `30.0.15729638`, produced:

`3A706062E1B3E804CA7F7ABBDEF523A05C4AD7C125AB1496E2B0A52A62C360A5`

This exactly matches `app/libs/libbox.aar`.

A build using NDK `28.0.13004108` does not produce the same binary hash.

## Embedded Go build information

Inspection of `jni/arm64-v8a/libbox.so` with:

`go version -m libbox.so`

reports:

- Go toolchain: `go1.26.6`
- module path: `github.com/sagernet/sing-box`
- module version: `(devel)`
- GOOS: `android`
- GOARCH: `arm64`
- build mode: `c-shared`
- trimpath: enabled

Build tags:

`with_gvisor,with_quic,with_wireguard,with_utls,with_naive_outbound,with_clash_api,badlinkname,tfogo_checklinkname0,with_tailscale,ts_omit_logtail,ts_omit_ssh,ts_omit_drive,ts_omit_taildrop,ts_omit_webclient,ts_omit_doctor,ts_omit_capture,ts_omit_kube,ts_omit_aws,ts_omit_synology,ts_omit_bird`

## Verification status

The provenance of the current legacy `libbox.aar` is considered recovered.

The following have been independently verified:

- upstream repository and release;
- upstream base commit;
- local platform-binding commit;
- exact additional uncommitted source change present at build time;
- Go version;
- gomobile version;
- Android NDK version required to reproduce the artifact;
- build command;
- build tags;
- supported ABIs;
- byte-for-byte SHA-256 reproduction of the final AAR.

## Policy for future libbox updates

Any replacement of `app/libs/libbox.aar` must record:

1. Exact upstream repository.
2. Exact sing-box tag and Git commit SHA.
3. Any local patches or commits.
4. Go toolchain version.
5. gomobile version.
6. Android NDK version.
7. Complete build command or reproducible build script.
8. Build tags.
9. SHA-256 of the resulting AAR.
10. Date of the update.

Future libbox builds should be made from a clean committed source tree. A new libbox artifact must not be committed without updating this provenance record.