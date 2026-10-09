# Spring Concepts
##### This document will not contain Java syntax, just mainly the concepts needed to implement the Spring framework
---

## Resources Used (Add On If Needed)
- Spring Boot Roadmap: https://roadmap.sh/spring-boot
- Spring Boot Youtube Playlist: https://www.youtube.com/watch?v=Zxwq3aW9ctU&list=PLsyeobzWxl7qbKoSgR5ub6jolI8-ocxCF&index=1&pp=iAQB
- Main Github Repo: https://github.com/conman4562/rpi-backtests
---

## IoC (Inversion of Control)
- IoC is a principle that Spring Boot follows as a part of its philosophy. It focuses on the developer giving up control and management of an instance or object to allow them to focus on the behavior and other OOP principles as opposed to the lifecycle and resource management. Spring handles the creation and cleanup of the resources and the developer focuses on implementing its functionality.

## Dependency Injection
- Dependency Injection is a method to implement IoC. We let Spring create the objects that we specify and inject these instances into other objects or functions that need it. There are 3 main types of dependency injection: constructor injection, setter injection, and field injection.
- Constructor Injection lets Spring pass the instance during the creation of another object or instance. This allows us only pass in information that a specific object needs instead of letting it have access to all instances that Spring creates.
- Setter Injection passes that instance into a function which allow isolated implementation by creating a method to "inject" a Spring instance into the object that needs access to this instance.
- Field Injection allows an object to freely access a specific object or instance within its class via auto wiring.

## JVM (Java Virtual Machine)
- The Java Virtual Machine allocates a region of memory for Spring to instantiate objects that the developer chooses to implement and create. It automatically manages the memory as well as the lifecycle of the objects.

## Spring Boot
- Back then, we had to write our code and then separately create a web server using Apache Tomcat to serve our backend. Now Spring boot handles that for you by automatically spinning up a Apache Tomcat instance for you to use. This follows the idea of IoC by allowing the developer to configure the web server, but give up management of the web server to Spring.

## MVC (Model-View-Controller)
- A common design pattern and architectural pattern for structuring the backend of a web application. It focuses on separating logic into its own "concerns". It stands for Model-View-Controller.
- In Model, we define schemas and how we would like to model our data. This is generally focused on how a database would store the information as well as structure and organize where information belongs.
- In Controller, we define our business logic or what we would like to do with information passed to the backend via HTTP methods. It accesses the models we've made and allows us to manipulate the views we would like to display or return back to the client's machine.
- In View, we focus on how we would like to display our data. Not necessarily aimed towards UI/UX, but more towards how data will be organized when the frontend recieves the information it needs from making its request to the backend. Serving pure HTML is a form of server side rendering that view accomplishes.

## Beans
- Application objects created and stored in the JVM are referred to as beans in Spring boot. It is an opinionated approach to structuring the backend by modularizing logic. This allows us to use dependency injection to control the flow of information in our backend. It follows the principle of Abstraction from OOP, by hiding implementation details and only passing what is needed to a specific "bean" in our case.

