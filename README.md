# economic-crime-levy-account

This is the backend microservice for getting data relating to a user's ECL account, such as obligation data and
financial details.

The service provides APIs to be consumed
by [economic-crime-levy-account-frontend](https://github.com/hmrc/economic-crime-levy-account-frontend) microservice as
well as the Business Tax Account service.

## API Endpoints

- [Get BTA tile data](api-docs/get-bta-tile-data.md): `GET /economic-crime-levy-account/bta-tile-data`

## Running the service

> `sbt run`

The service runs on port `14009` by default.

## Running dependencies

Using [service manager](https://github.com/hmrc/service-manager)
with the service manager profile `ECONOMIC_CRIME_LEVY_ALL` will start
all of the Economic Crime Levy microservices as well as the services
that they depend on.

> `sm --start ECONOMIC_CRIME_LEVY_ALL`

## Running tests

### Unit tests

> `sbt test`

### Integration tests

> `sbt it:test`

### All tests

This is an sbt command alias specific to this project. It will run a scala format
check, run a scala style check, run unit tests, run integration tests and produce a coverage report.
> `sbt runAllChecks`

## Scalafmt and Scalastyle

To check if all the scala files in the project are formatted correctly:
> `sbt scalafmtCheckAll`

To format all the scala files in the project correctly:
> `sbt scalafmtAll`

To check if there are any scalastyle errors, warnings or infos:
> `sbt scalastyle`

### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").