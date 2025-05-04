# OTP Verification Service

Простой backend-сервис для защиты операций с помощью одноразовых кодов (OTP).  
Поддерживает генерацию, доставку и валидацию кодов через SMS, Email, Telegram и файл.

---

## 📌 Что умеет сервис

- Регистрация и авторизация пользователей с ролями **ADMIN** и **USER**
- Генерация OTP-кодов для подтверждения операций
- Поддержка доставки кодов по:
  - 📱 SMS (эмуляция)
  - 📧 Email (эмуляция)
  - 💬 Telegram (эмуляция)
  - 🗂 Сохранение в файл
- Валидация OTP-кодов
- Административное API:
  - Изменение длины и срока жизни кода
  - Управление пользователями

---

## 🚀 Как запустить

> Требуется Java 17+ и Maven

```bash
git clone https://github.com/yourusername/otpservice.git
cd otpservice
mvn clean install
java -cp target/otpservice-1.0-SNAPSHOT.jar ServerApplication
```

Сервер стартует на порту `8080`.

---

## 🔐 API: Команды

### 🧑 Регистрация и логин

```http
POST /auth/register
username=alice&password=1234&role=USER

POST /auth/login
username=alice&password=1234
→ Возвращает токен
```

---

### 🔒 Генерация и валидация OTP

```http
POST /user/otp
Headers: Authorization: <token>
operationId=test123&deliveryMethod=EMAIL

POST /user/validate
Headers: Authorization: <token>
operationId=test123&code=123456
```

---

### 🛠 API администратора (только для ADMIN)

```http
POST /admin/otp-config
Headers: Authorization: <admin-token>
codeLength=6&codeLifetimeSeconds=300

GET /admin/users
Headers: Authorization: <admin-token>

DELETE /admin/users/username
Headers: Authorization: <admin-token>
```

---

## ✅ Как протестировать

1. Зарегистрируйте обычного пользователя и админа.
2. Получите токены через `/auth/login`.
3. Выполните `/user/otp` с `deliveryMethod=SMS|EMAIL|TELEGRAM|FILE`.
4. Проверьте файлы:
   - `sms_notifications.txt`
   - `email_notifications.txt`
   - `telegram_notifications.txt`
   - или файл в корне с OTP-кодом
5. Выполните `/user/validate` с полученным кодом.

> Все OTP-коды живут заданное количество секунд (по умолчанию — 300).  
> Коды автоматически получают статус `EXPIRED`, если не используются вовремя.

---

## 📦 Технологии

- Java 17
- Maven
- PostgreSQL (JDBC)
- `com.sun.net.httpserver.HttpServer`
- `java.util.logging`

---
