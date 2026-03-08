# Smart Waste Management System - Product Requirements Document (PRD)

## 1. Introduction
The Smart Waste Management System is a comprehensive platform designed to optimize waste collection, recycling, and disposal processes. It connects individual users, property managers, drivers, and administrators to ensure efficient, eco-friendly waste management.

## 2. User Roles
### 2.1 Existing Roles
- **Individual User (C2C/B2C)**: Creates on-demand orders, tracks status, views personal history.
- **Driver**: Receives tasks, navigates to pickup points, updates order status (Load/Unload).
- **Administrator**: Global system monitoring, station/fleet management, master data control.

### 2.2 New Roles (To Be Implemented)
- **Property Manager (B2B)**: Represents residential communities or commercial buildings. Manages bulk waste collection, views aggregate statistics, and handles monthly billing/subscriptions.
- **VIP User**: High-priority individual users with faster response times and dedicated support.

## 3. Business Models
### 3.1 Existing Models
- **On-Demand (Pay-per-use)**: Users pay for each specific pickup order based on weight and type.

### 3.2 New Models (To Be Implemented)
- **Subscription (SaaS/Service)**: Users (especially B2B) pay a monthly fee for regular, scheduled pickups (e.g., "Weekly Pickup Plan").
- **B2B Contracts**: Long-term contracts with property management firms for discounted rates and priority service.

## 4. Functional Requirements

### 4.1 Property Manager Portal (B2B)
- **Dashboard**: View total waste collected for the community, cost trends, and upcoming scheduled pickups.
- **Bulk Order**: Create orders for multiple locations within the property.
- **Subscription Management**: Subscribe to weekly/monthly plans.

### 4.2 Subscription System
- **Plan Selection**: Basic (Weekly), Premium (Daily), Enterprise (Custom).
- **Auto-Renewal**: System automatically generates pickup tasks based on subscription frequency.

## 5. Technical Architecture
- **Frontend**: Vue 3 + Element Plus (Responsive).
- **Backend**: Spring Boot 3 + H2 Database.
- **Deployment**: Dockerized containers.

## 6. Success Metrics
- **User Coverage**: 100% support for Individual, Driver, Admin, and Property Manager roles.
- **Traceability**: All requirements mapped to code implementation.
