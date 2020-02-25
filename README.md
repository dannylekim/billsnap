# BillSnap: The Bill Splitting and Roommate Management System 
![BCH compliance](https://bettercodehub.com/edge/badge/dannylekim/billsnap?branch=develop&token=0d87e307153724f90e74a95254e4fd924d8269be)
[![CodeFactor](https://www.codefactor.io/repository/github/dannylekim/billsnap/badge?s=d03236e11106ad11acbf82ea016e056275ba27fb)](https://www.codefactor.io/repository/github/dannylekim/billsnap)
[![codecov](https://codecov.io/gh/dannylekim/billsnap/branch/develop/graph/badge.svg?token=XP6BVqx6XT)](https://codecov.io/gh/dannylekim/billsnap)

BillSnap is all about snapping away full bills into dust.

In essence, BillSnap is a Bill Splitting and Roommate management application.

The goal is to allow ease of splitting bills with who ordered what or which roommate needs to pay which bill and at which occurrence.


## Prerequisites

- Java 13 or higher

## Getting Started

First clone the project with the command

```git clone https://github.com/dannylekim/billsnap.git```

and then build the dependencies using gradle

```./gradlew assemble``` 

Afterwards, you can simply run the application with 

```./gradlew bootRun```

and it will launch the application at 

`localhost:8000/billsnap/swagger-ui.html`.

Swagger is a useful tool that gives our RESTful APIs documentation and an ease to try out the code without using other 
applications such as Postman. 

Make sure to checkout [Wiki](https://github.com/dannylekim/billsnap/wiki) to learn more about workflows and the project!

## Contributing to the Repo

The collaborators must use [ClickUp](https://app.clickup.com/1276317/v/l/li/16947525) which is a project management app.

To contribute to the repo, simply import this to your favorite IDE (such as Jetbrains IntelliJ) 
and go onto the click up application to pick a sub-task within the sprint and check out the branch in the order of

```{keyword}/{taskId}_{subTaskId}``` 

and start coding away! 
