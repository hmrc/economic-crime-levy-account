# economic-crime-levy-account

This is the backend microservice for getting data relating to a user's ECL account, such as obligation data and
financial details.

The service provides APIs to be consumed by other Economic Crime Levy services as well as the Business Tax Account
service.

## API Endpoints

- [Get BTA tile data](api-docs/get-bta-tile-data.md): `GET /economic-crime-levy-account/bta-tile-data`

## Running dependencies

Using [sm2](https://github.com/hmrc/sm2)
with the service manager profile `ECONOMIC_CRIME_LEVY_ALL` will start
all the Economic Crime Levy microservices as well as the services
that they depend on.

```
sm2 --start ECONOMIC_CRIME_LEVY_ALL
```

To stop the microservice from running on service manager (e.g. to run your own version locally), you can run:

```
sm2 --stop ECONOMIC_CRIME_LEVY_ACCOUNT 
```


### Using localhost

To run this microservice locally on the configured port **'14009'**, you can run:

```
sbt run 
```

**NOTE:** Ensure that you are not running the microservice via service manager before starting your service locally (vice versa) or the service will fail to start


### Accessing the service

Access details can be found on
[DDCY Live Services Credentials sheet](https://docs.google.com/spreadsheets/d/1ecLTROmzZtv97jxM-5LgoujinGxmDoAuZauu2tFoAVU/edit?gid=1186990023#gid=1186990023)
for both staging and local url's or check the Tech Overview section in the
[service summary page ](https://confluence.tools.tax.service.gov.uk/display/ELSY/ECL+Service+Summary)


## Running tests

### Unit tests

> `sbt test`

### Integration tests

> `sbt it:test`

### All tests

This is a sbt command alias specific to this project. It will run a scala format
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


## Monitoring

The following grafana and kibana dashboards are available for this service:

* [Grafana](https://grafana.tools.production.tax.service.gov.uk/d/economic-crime-levy-account/economic-crime-levy-account?orgId=1&from=now-24h&to=now&timezone=browser&var-ecsServiceName=ecs-economic-crime-levy-account-protected-Service-Lk7QibiQP6IF&var-ecsServicePrefix=ecs-economic-crime-levy-account-protected&refresh=15m)
* [Kibana](https://kibana.tools.production.tax.service.gov.uk/app/dashboards#/view/economic-crime-levy-account?_g=(filters:!(),refreshInterval:(pause:!t,value:60000),time:(from:now-15m,to:now))


## Other helpful documentation

* [Service Runbook](https://confluence.tools.tax.service.gov.uk/display/ELSY/Economic+Crime+Levy+%28ECL%29+Runbook)

* [Architecture Links](https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=859504759)