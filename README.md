# Version Control System Services

This service is an API Gateway to various Version Control Systems.
### Supported VCSs
- GitHub - with personal token

## Requirements
- Java 17
- Maven

## Running
As this is regular Spring Boot Web application, you could run it via main method as usual. It will run a server with port 8080 exposed. To see available endpoints check [`swagger.yaml`](swagger.yaml). Alternatively built jar and run it:
```bash
$ mvn clean install
$ java -jar ./vcsservices-app/target/vcsservices-app-<version>.jar
```
Or build an docker image and run it
``` bash
$ mvn clean install
$ docker build -t smakhov/vcsservices .
$ docker run -p 8080:8080 smakhov/vcsservices
```

*IMPORTANT*: GithHub API limits unauthenticated calls to 60 per hour. To increase this limit pass your `Personal access token` as `config.clients.github.authToken` environment variable. For more information see [Github API documentation](https://docs.github.com/en/developers/apps/building-github-apps/rate-limits-for-github-apps)
