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

## Autowiring
- Without Spring Boot, the developer needs to manually configure which class instances should be created on the JVM. These beans are then injected into other beans in the xml file. However, the Spring framework is actually smart enough to understand that it can automatically figure out which bean needs to be injected via autowiring. By simply specifying if we want to autowire by type or by name (id), we won't need to manually inject the bean in our source code and simply have Spring inject it on launch. To avoid confusion for Spring, we can use the primary attribute to default to a specific bean in the case of name collision.

## Servlets
- In Java web applications, we need a middleman to handle client requests and server side responses. Servlets are server side Java programs that run within a Servlet Container, such as Apache Tomcat, in order to assist in processing request and responses. This methodology is now abstracted in Spring Boot.

## REST (Representational State Transfer)
- REST APIs is a style of API that allows HTTP communication. As its name suggests, it transfers state or data from the frontend to the backend and vice versa. In SpringBoot, we can specifically specify a controller as adhering to the REST API constraints or just specify that a method is expected to just return data, not a layout.

## Front Controller
- Further discussing the layer known as the controller layer, there is actually a separate layer called the Front Controller in front of the controller layer that you will build. This front controller acts like a router that chooses which controller to send the incoming HTTP request to.

## N-Layered Architecture
- The main architectural pattern that Spring Boot uses is known as MVC. However, in the case of writing enterprise level applications, we can't just put everything into controllers as the controller's sole purpose is just to process the request and return the response, not actually conduct any business logic. Methodology issues like this breed styles of the architectural pattern to fit the scale of the application. One of these styles is known as Layered Architecture or N-Layered Architecture. It focuses on separating mainly the controller in different layers such that when a simple request is processed and each step of processing is separated by layers. Many different applications have different principles behind how many layers or what each layer should do, but for Spring Boot, it starts with Controller, Services, and Respository.

## CRUD (Create, Read, Update, Delete)
- CRUD describes the fundamental operations that a software application must adhere to if it persists data or interacts with a database. Every web application that uses a database implements routes/endpoints to allow CRUD operations.

## Path Parameters/Route Parameters
- One of the many ways to transfer state in a web application is through the URL of the endpoint itself. Path parameters allow the client to specify specific filters to be passed to the controller for further business logic. It is a primitive type whose location in the request mapping can be configured in the controller itself. This method of transfer is strictly encouraged to only be implemented in GET and DELETE requests.

## Request Body
- The other way data is transferred is through a request body. The body can be any type ranging from plain text to JSON. In the case of Spring Boot and general languages that don't have native support for JSON, we often create immutable classes known as data classes that are structured based on the expected request body to store the information. For Spring Boot, we can just specify this class as Data object so that the JVM initializes it when needed. No autowiring required as this is technically a form of setter injection into the controller method.

## ORM (Object Relational Mapping)
- As developers, we want to abstract as much as we can so that we can focus on building the actual logic. In the case of interacting with the database, we don't really want to have to write code in another language and then have to implement security safeguards against attacks such as SQL injections. An ORM tool handles all of that extra conversion and security for us and allows us to stay in Java while writing the logic that will be converted to the appropriate database language. As the name implies, we map a Java object (bean) to a relational database table, thereby, abstracting the need to actually write SQL.

## JPA (Java Persistance API)
- This is just a list of standards that ORM tools have to follow for their purpose. This makes it easier to migrate and learn different ORM tools. The most popular ORM tool is Hibernate so this project will be using that. In addition, Spring provides a special module to implement JPA called Spring Data JPA which abstracts the repository layer so that we can write less code.

## CORS (Cross Origin Resource Sharing)
- For security reasons, by default it is not allowed to access different applications on different ports. This prevents the frontend from interacting with the backend. We need to now specify in the backend which URLs are allowed to access this backend.

## ACID (Atomicity, Consistency, Isolation, Durability)
-

## Atomicity
- The ACID principles are a set of guidelines for reliable database transactions. However, we have already abstracted the layer that handles interacting with the database to Spring Data JPA. Despite this, we want to make sure our logic is also atomic as the service layer could potentially run multiple different methods from the repository layer. The idea behind atomicity is that every action is all or nothing. If one part of the service fails, no logic is partially executed and every change that has happened should be rolled back. This ensures that we are appropriately handing what each request is doing and failing correctly if the entire purpose isn't fulfilled.

