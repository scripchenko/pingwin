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

## Upstream revision status

The exact upstream sing-box tag or Git commit used to produce the current AAR is not recoverable from the artifact metadata currently available.

The embedded Go module is recorded as `github.com/sagernet/sing-box (devel)`, so version-like strings found inside the native library must not be treated as proof of the sing-box release version.

Therefore the current AAR is treated as a pinned legacy binary identified by its SHA-256 digest.

## Policy for future libbox updates

Any replacement of `app/libs/libbox.aar` must record:

1. Exact upstream repository.
2. Exact sing-box tag and Git commit SHA.
3. Go toolchain version.
4. Complete build command or reproducible build script.
5. Build tags.
6. SHA-256 of the resulting AAR.
7. Date of the update.

A new libbox artifact must not be committed without updating this provenance record.
