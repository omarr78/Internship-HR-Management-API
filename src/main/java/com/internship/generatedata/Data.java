package com.internship.generatedata;

import static com.internship.enums.Gender.FEMALE;
import static com.internship.enums.Gender.MALE;

public class Data {
    static final Person[] PEOPLE = {
            new Person("Mohamed", MALE), new Person("Ahmed", MALE), new Person("Mahmoud", MALE),
            new Person("Mostafa", MALE), new Person("Omar", MALE), new Person("Youssef", MALE),
            new Person("Karim", MALE), new Person("Ibrahim", MALE), new Person("Khaled", MALE),
            new Person("Tamer", MALE), new Person("Amr", MALE), new Person("Hossam", MALE),
            new Person("Sherif", MALE), new Person("Wael", MALE), new Person("Mina", MALE),
            new Person("Bassem", MALE), new Person("Ayman", MALE), new Person("Islam", MALE),
            new Person("Fady", MALE), new Person("Sameh", MALE), new Person("Adel", MALE),
            new Person("Samir", MALE), new Person("Nader", MALE), new Person("Essam", MALE),
            new Person("Saber", MALE), new Person("Ehab", MALE), new Person("Medhat", MALE),
            new Person("Emad", MALE), new Person("Ashraf", MALE), new Person("Magdy", MALE),
            new Person("Farouk", MALE), new Person("Galal", MALE), new Person("Ragab", MALE),
            new Person("Shady", MALE), new Person("Maged", MALE), new Person("Ramy", MALE),
            new Person("Hazem", MALE), new Person("Atef", MALE), new Person("Saad", MALE),
            new Person("Hany", MALE), new Person("Mariam", FEMALE), new Person("Fatma", FEMALE),
            new Person("Aya", FEMALE), new Person("Salma", FEMALE), new Person("Nour", FEMALE),
            new Person("Yasmin", FEMALE), new Person("Habiba", FEMALE), new Person("Menna", FEMALE),
            new Person("Hagar", FEMALE), new Person("Dina", FEMALE), new Person("Reem", FEMALE),
            new Person("Rania", FEMALE), new Person("Shaimaa", FEMALE), new Person("Nada", FEMALE),
            new Person("Doaa", FEMALE), new Person("Heba", FEMALE), new Person("Esraa", FEMALE),
            new Person("Mai", FEMALE), new Person("Amal", FEMALE), new Person("Ghada", FEMALE),
            new Person("Noura", FEMALE), new Person("Samar", FEMALE), new Person("Manal", FEMALE),
            new Person("Iman", FEMALE), new Person("Sawsan", FEMALE), new Person("Farah", FEMALE),
            new Person("Asmaa", FEMALE), new Person("Marwa", FEMALE), new Person("Basma", FEMALE),
            new Person("Riham", FEMALE), new Person("Abeer", FEMALE), new Person("Nagwa", FEMALE),
            new Person("Walaa", FEMALE), new Person("Eman", FEMALE), new Person("Ola", FEMALE),
            new Person("Sahar", FEMALE), new Person("Hend", FEMALE), new Person("Hala", FEMALE),
            new Person("Laila", FEMALE), new Person("Dalia", FEMALE), new Person("Omnia", FEMALE),
            new Person("Nermin", FEMALE), new Person("Shorouk", FEMALE), new Person("Radwa", FEMALE),
            new Person("Rasha", FEMALE), new Person("Huda", FEMALE), new Person("Karima", FEMALE),
            new Person("Safaa", FEMALE), new Person("Yomna", FEMALE), new Person("Soha", FEMALE),
            new Person("Amira", FEMALE), new Person("Donia", FEMALE), new Person("Wafaa", FEMALE),
            new Person("Sabah", FEMALE), new Person("Zainab", FEMALE), new Person("Maha", FEMALE),
            new Person("Lobna", FEMALE), new Person("Kawthar", FEMALE), new Person("Thanaa", FEMALE),
            new Person("Faten", FEMALE)
    };

    static final String[] LAST_NAMES = {
            "Mohamed", "Ahmed", "Mahmoud", "Mostafa", "Omar", "Youssef", "Karim", "Ibrahim",
            "Khaled", "Tamer", "Amr", "Hossam", "Sherif", "Wael", "Mina", "Bassem",
            "Ayman", "Islam", "Fady", "Sameh", "Adel", "Samir", "Nader", "Essam",
            "Saber", "Ehab", "Medhat", "Emad", "Ashraf", "Magdy", "Farouk", "Galal",
            "Ragab", "Shady", "Maged", "Ramy", "Hazem", "Atef", "Saad", "Hany",
            "Ali", "Hassan", "Hussein", "Zaid", "Ziad", "Yassin", "Hamza", "Marwan",
            "Anas", "Adam", "Malik", "Rayan", "Seif", "Tariq", "Yahya", "Yaser",
            "Nabil", "Faris", "Bilal", "Samy", "Hadi", "Fouad", "Ramadan", "Shaaban",
            "Gamal", "Nasr", "Said", "Talaat", "Raafat", "Fikry", "Zaki", "Anwar",
            "Abdelrahman", "Abdallah", "Mostafa", "Moussa", "Issa", "Suleiman", "Dawood", "Zakaria",
            "Bahaa", "Alaa", "Diaa", "Mounir", "Wagdi", "Refaat", "Shawky", "Hamdy",
            "Lotfy", "Sobhy", "Kamal", "Galal", "Fayez", "Fawzy", "Bahgat", "Tharwat",
            "Sultan", "Mansour", "Gomaa", "Kamel", "Saeed", "Reda", "Fahmy", "Hatem",
            "Ammar", "Moataz", "Tamim", "Ezz"
    };

    static final String[] DEPARTMENTS = {
            "Software Engineering", "Product Management", "Quality Assurance", "DevOps & Infrastructure",
            "User Experience (UX) Design", "Data Science & Analytics", "Cybersecurity", "Customer Success",
            "Technical Support", "Sales & Business Development", "Marketing", "Human Resources", "Finance",
            "Legal & Compliance", "Project Management Office", "Research & Development", "Solutions Architecture",
            "Information Technology", "Professional Services", "Public Relations"
    };

    static final String[] TEAMS = {
            "Core Backend Squad", "Frontend Architects", "Mobile Innovators", "Cloud Pioneers",
            "Data Miners", "Security Sentinels", "API Integrators", "QA Ninjas",
            "The Debuggers", "AI Explorers", "Velocity Squad", "DevOps Gladiators",
            "Full Stack Fusion", "User Journey Map", "Legacy Legends", "Platform Reliability",
            "Agile Alchemists", "Code Crafters", "The Patch Crew", "Future Tech Lab"
    };

    static final String[][] EXPERTISES = {
            {"Java Streams & Lambdas", "JUnit 5 & Mockito Testing", "Maven & Gradle Build Systems", "REST API Basics", "HTML5 & CSS3", "Angular Components", "Git Version Control", "SQL Fundamentals", "TypeScript Basics", "Java Collections Framework"},
            {"Spring Boot Microservices", "Spring Data JPA & Hibernate", "Angular Reactive Forms", "Spring Security Basics", "PostgreSQL Optimization", "RxJS Observables", "JWT Authentication", "Integration Testing", "Clean Code Principles", "WebSockets"},
            {"Docker & Kubernetes", "Redis Caching Strategies", "SQL Query Optimization", "Spring Cloud Gateway", "NoSQL Integration (MongoDB)", "CI/CD Pipeline Automation", "Design Patterns", "State Management (NgRx)", "Microservices Service Discovery", "Cloud Native Development"},
            {"Multi-threading & Concurrency", "Apache Kafka & RabbitMQ", "JVM Tuning & Garbage Collection", "Aspect-Oriented Programming", "System Scalability & High Availability", "Event-Driven Architecture", "OAuth2 & OpenID Connect", "Distributed Systems Design", "Infrastructure as Code", "Enterprise Integration Patterns"}
    };
}
