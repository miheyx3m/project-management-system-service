Project Management System

- project-management-system-service
    - controller for manage PM System
    - save data in DB H2(db updated every start the service)
    - The Project hierarchy data model is:
        {
        "uid" : "String",
        "name" : "String",
        "type" : "String", // {“TASK”, “PROJECT”} 
        "startDate" : "Date",
        "endDate" : "Date",
        “children” : ”List<Subproject/Task>”
        }
      
- Swagger endpoint:
    - http://localhost:8080/swagger-ui/
