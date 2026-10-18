# Spring 0.1.0 external-consumer verification

Verified on 2026-10-18 against release tag
[`v0.1.0`](https://github.com/kazforge/jsonapi-java-spring/releases/tag/v0.1.0),
commit `6935b7d62a5911937bd45a98f985274cfc13640b`.
The tagged guide/example were inspected before wiring this consumer.

## Clean consumer build

Run from the demo root with Java 21 (locally: Temurin 21.0.12+8):

```sh
java -version
export GRADLE_USER_HOME="$(mktemp -d /tmp/jsonapi-spring-consumer.XXXXXX)"
./gradlew --no-daemon clean build
./gradlew --no-daemon :examples:spring-webmvc:dependencies --configuration runtimeClasspath
./gradlew --no-daemon :examples:spring-webmvc:dependencyInsight --configuration runtimeClasspath --dependency jsonapi-java-api
./gradlew --no-daemon :examples:spring-webmvc:dependencyInsight --configuration runtimeClasspath --dependency jsonapi-java-query
```

No development-repository artifacts or Gradle caches are used. Settings enforce
Maven Central for plugins and dependencies and reject project repositories.
There is no Maven Local, composite build, project substitution, local publication
or source dependency. The root `clean build` includes both independent examples.
CI explicitly creates an empty `GRADLE_USER_HOME`, uses Java 21, prints the
runtime graph and uploads both examples' test reports.

Both builds passed: **11 Spring integration cases and 4 Jackson basic cases**,
zero failures/errors. The first build used a new empty Gradle home and
`./gradlew --no-daemon spotlessApply clean build`; the subsequent unmodified
`clean build` also passed. The Gradle-resolved Spring JAR has the same SHA-256
as the independently downloaded public Central JAR below.

Resolved runtime graph (relevant paths, no direct API/query declaration):

```text
com.kazforge:jsonapi-java-spring-webmvc:0.1.0
+-- com.kazforge:jsonapi-java-api:0.4.0
|   `-- com.kazforge:jsonapi-java-core:0.4.0
+-- com.kazforge:jsonapi-java-query:0.4.0
|   `-- com.kazforge:jsonapi-java-api:0.4.0
+-- org.springframework:spring-webmvc:7.0.9
`-- jakarta.servlet:jakarta.servlet-api:6.1.0
com.kazforge:jsonapi-java-jackson3:0.4.0
+-- com.kazforge:jsonapi-java-api:0.4.0
+-- com.kazforge:jsonapi-java-annotations:0.4.0
+-- com.kazforge:jsonapi-java-core:0.4.0
`-- tools.jackson.core:jackson-databind:3.2.3 (application override of 3.1.7)
    +-- tools.jackson.core:jackson-core:3.2.3
    `-- com.fasterxml.jackson.core:jackson-annotations:2.22
```

Spring introduces **no Jackson runtime**. Jackson 3 comes only from the explicit
application dependencies. Its shared `jackson-annotations:2.22` is expected;
there is no Jackson 2 core/databind, `jsonapi-java-jackson2`, Boot, or separate
mapping artifact. `dependencyInsight` confirmed API/query `0.4.0` are transitive
dependencies of the published Spring adapter, with no version substitutions.

## Public Central retrieval

An upload success alone is not retrieval evidence. Independently retrieve the
actual files (do not rely on the version directory listing):

```sh
export CENTRAL=https://repo.maven.apache.org/maven2/com/kazforge/jsonapi-java-spring-webmvc
export CHECK_DIR="$(mktemp -d /tmp/jsonapi-spring-central.XXXXXX)"
curl --fail --silent --show-error "$CENTRAL/maven-metadata.xml" -o "$CHECK_DIR/maven-metadata.xml"
for suffix in .pom .jar .module -sources.jar -javadoc.jar -cyclonedx.json; do
  file="jsonapi-java-spring-webmvc-0.1.0$suffix"
  for item in "$file" "$file.asc" "$file.sha256"; do
    curl --fail --silent --show-error --write-out 'HTTP %{http_code} %{url_effective}\n' \
      "$CENTRAL/0.1.0/$item" -o "$CHECK_DIR/$item" || exit 1
  done
  printf '%s  %s\n' "$(cat "$CHECK_DIR/$file.sha256")" "$CHECK_DIR/$file" | sha256sum --check || exit 1
done
for suffix in .jar -sources.jar -javadoc.jar; do
  unzip -t "$CHECK_DIR/jsonapi-java-spring-webmvc-0.1.0$suffix" || exit 1
done
```

Exact primary URLs:

- [POM](https://repo.maven.apache.org/maven2/com/kazforge/jsonapi-java-spring-webmvc/0.1.0/jsonapi-java-spring-webmvc-0.1.0.pom)
- [JAR](https://repo.maven.apache.org/maven2/com/kazforge/jsonapi-java-spring-webmvc/0.1.0/jsonapi-java-spring-webmvc-0.1.0.jar)
- [Gradle metadata](https://repo.maven.apache.org/maven2/com/kazforge/jsonapi-java-spring-webmvc/0.1.0/jsonapi-java-spring-webmvc-0.1.0.module)
- [Sources](https://repo.maven.apache.org/maven2/com/kazforge/jsonapi-java-spring-webmvc/0.1.0/jsonapi-java-spring-webmvc-0.1.0-sources.jar)
- [Javadocs](https://repo.maven.apache.org/maven2/com/kazforge/jsonapi-java-spring-webmvc/0.1.0/jsonapi-java-spring-webmvc-0.1.0-javadoc.jar)
- [CycloneDX SBOM](https://repo.maven.apache.org/maven2/com/kazforge/jsonapi-java-spring-webmvc/0.1.0/jsonapi-java-spring-webmvc-0.1.0-cyclonedx.json)
- [Version metadata](https://repo.maven.apache.org/maven2/com/kazforge/jsonapi-java-spring-webmvc/maven-metadata.xml)

All six files, their `.asc` signatures and `.sha256` checksums returned HTTP
200; checksums matched and all three JAR archives passed integrity checks.
Version metadata lists `0.1.0` as latest/release, last updated
`20261018204854`. The version directory listing itself returned HTTP 404;
this does **not** prevent direct file retrieval or Gradle resolution.

Primary JAR SHA-256:
`2f28227116ffefe87a06a0c6700b55443cb369f21c6329aab4c80bed35852310`.

## Signatures, SBOM and provenance

With the `CHECK_DIR` above, use an isolated keyring:

```sh
export GNUPGHOME="$(mktemp -d /tmp/jsonapi-spring-gpg.XXXXXX)"
curl --fail --silent --show-error \
  'https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x8A6402C7B2BEF99C01726C825E9C911789EC1FE2' \
  -o "$CHECK_DIR/signing-key.asc"
gpg --import "$CHECK_DIR/signing-key.asc"
gpg --fingerprint 5E9C911789EC1FE2
for suffix in .pom .jar .module -sources.jar -javadoc.jar -cyclonedx.json; do
  file="$CHECK_DIR/jsonapi-java-spring-webmvc-0.1.0$suffix"
  gpg --verify "$file.asc" "$file" || exit 1
done
jq '{bomFormat, specVersion, component: .metadata.component, components: [.components[] | {group, name, version}]}' \
  "$CHECK_DIR/jsonapi-java-spring-webmvc-0.1.0-cyclonedx.json"
```

All six detached signatures verified with key fingerprint
`8A6402C7B2BEF99C01726C825E9C911789EC1FE2`
(`KazForge Release <jsonapi@kazforge.com>`). This proves signature integrity,
not independent certification of the key owner's identity: GPG reports unknown
trust. The key was available from Ubuntu's keyserver; keys.openpgp.org's
key-ID endpoint returned 404.

The published CycloneDX 1.6 SBOM identifies Spring `0.1.0`, Core API/query
`0.4.0`, Spring `7.0.9`, Servlet `6.1.0` and no Jackson runtime. Its build
reference points to the [successful release workflow](https://github.com/kazforge/jsonapi-java-spring/actions/runs/39163602504).
That workflow's upload is separate evidence from the public retrieval above.

GitHub's digest-indexed attestation API also exposes SBOM and SLSA provenance
statements matching the downloaded JAR hash and release commit:

```sh
gh api repos/kazforge/jsonapi-java-spring/attestations/sha256:2f28227116ffefe87a06a0c6700b55443cb369f21c6329aab4c80bed35852310 \
  --jq '.attestations[].bundle.dsseEnvelope.payload | @base64d | fromjson | {predicateType, subject}'
```

Those statements were inspected, not cryptographically verified: the installed
GitHub CLI lacks `gh attestation verify`. The six Maven PGP signatures above
were cryptographically verified. No release assets are attached to the GitHub
Release; the signed sources/Javadocs/SBOM are available from Central instead.
