# Banking-rest-project
Spring Boot REST API для банковских операций. Управление клиентами, картами и транзакциями через HTTP запросы.

## Технологии
- Java 17 + Spring Boot 3.x
- Spring Data JPA + H2 Database
- Spring MVC (REST архитектура)
- Maven, JUnit 5, Mockito

## Основные функции
- RESTful API для клиентов, карт и транзакций
- Пагинация для списков транзакций
- Автоматическая генерация номеров карт с проверкой уникальности
- Транзакционная логика для денежных операций

## Структура базы данных
Основные сущности в базе данных:
- **Client** - информация о клиентах банка
- **Card** - банковские карты с привязкой к клиентам
- **Transaction** - история всех финансовых операций

## Запуск приложения
### Требования
Java 17 или выше
Maven 3.6+
PostgreSQL 
### Клонировать репозиторий
```
git clone https://github.com/Nyaggets/banking-rest-project.git
cd banking-rest-project
```
### Запустить проект
```
mvn spring-boot:run
```
Приложение будет доступно по адресу: http://localhost:8080

### Примеры запросов
```
POST /clients/1/cards/create   //Выпуск новой карты
GET  /cards/123/transactions  //История транзакций карты
POST /cards/123/transactions/createTransfer  //Перевод между картами
```

