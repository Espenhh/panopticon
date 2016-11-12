# panopticon backend

This is the backend application used to run panopticon.
It contains APIs to receive status updates from running applications, as well as APIs for the panopticon frontend to use when showing the dashboard.

## How to run locally from command line

* `mvn clean install`
* `mvn spring-boot:run`

## How to run locally from IntelliJ

* Run the `main` method in `no.panopticon.Application`

## Test the API with Postman

* Import the file `panopticon.postman_collection.json` in [https://www.getpostman.com](Postman) to get a full list of possible API calls.
* The folder `External` are the APIs that applications will use to send in their statuses.
* The folder `Internal` are the APIs that the panopticon frontend will use to display the dashboard.