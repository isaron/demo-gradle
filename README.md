# demo-gradle

Demo Spring Cloud project deploying on K8s. Support building container images that doesn't depend on a Docker daemon.

Something as:

1. Spring Cloud project with Gradle.
2. Use Jib(https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin#quickstart) building container images that doesn't depend on a Docker daemon. And we can build images with Docker, of cource.
3. Jenkins declarative pipeline for standardized workflow CI-CD.
4. Support deploying project on K8s cluster, with modified Helm charts. By default, support Ingress, Certs, auto-generated DNS domains, etc.
5. Support Skaffold for uniform build system. (In progress)

This could be a project template.
