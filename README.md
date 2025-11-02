# 🛒 E-commerce Order Management System

This project is a **Spring Boot-based RESTful API** for managing products and orders in an e-commerce platform.  
It supports product management, order processing, stock control, and order status transitions (Pending → Confirmed → Shipped → Delivered → Cancelled).

---

## 🚀 Features

- **Product Management**: Create, update, list, and delete products.
- **Order Management**: Create and manage customer orders.
- **Stock Control**: Automatically reduces or restores stock on order confirmation or cancellation.
- **Validation**: Uses `@Valid`, `@NotNull` annotations for request validation.
- **Logging**: SLF4J + Logback integrated for key operations.
- **Profile-based Configuration**: Supports multiple environments (dev, test, prod).
- **Transactional Operations**: Ensures data consistency with `@Transactional`.

---

## 🧩 Tech Stack

| Layer | Technology |
|-------|-------------|
| Backend Framework | Spring Boot |
| Database | PostgreSQL |
| ORM | Hibernate / JPA |
| Validation | Jakarta Bean Validation |
| Build Tool | Maven |
| Logging | SLF4J + Logback |
| API Testing | Postman |

---

## ⚙️ Installation & Run Guide

### 1. Clone the repository
```bash
git clone https://github.com/Aliissomeoneelse/ecommerce-order-management.git
cd ecommerce-order-management

📦 API Endpoints
| Method   | Endpoint                                   | Description         |
| -------- | ------------------------------------------ | ------------------- |
| `GET`    | `/api/products`                            | List all products   |
| `POST`   | `/api/products`                            | Create new product  |
| `PUT`    | `/api/products/{id}`                       | Update product      |
| `DELETE` | `/api/products/{id}`                       | Deactivate product  |
| `GET`    | `/api/orders`                              | List all orders     |
| `POST`   | `/api/orders`                              | Create new order    |
| `PUT`    | `/api/orders/{id}/status?status=CONFIRMED` | Change order status |
| `DELETE` | `/api/orders/{id}`                         | Cancel order        |
