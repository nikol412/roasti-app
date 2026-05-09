# Roasti Server

Ktor-бэкенд на Kotlin, замена Go-сервера.

## Запуск

### 1. Запусти Firebase эмулятор

```bash
./gradlew :server:firebaseEmulator
```

UI эмулятора: http://localhost:4000

### 2. Запусти сервер

В отдельном терминале:

```bash
./gradlew :server:serverDev
```

Сервер поднимется на http://localhost:8080

## Проверка компиляции всех модулей

```bash
./gradlew assembleDebug :server:compileKotlin
```

## Переменные окружения

| Переменная                        | По умолчанию (dev)                                                      | Описание                              |
|-----------------------------------|-------------------------------------------------------------------------|---------------------------------------|
| `FIREBASE_AUTH_EMULATOR_HOST`     | `localhost:9099`                                                        | Адрес Firebase эмулятора              |
| `FIREBASE_API_KEY`                | `test`                                                                  | API ключ Firebase                     |
| `FIREBASE_CREDENTIALS_JSON_BASE64`| —                                                                       | Service account (не нужен в dev)      |
| `FIREBASE_IDENTITY_BASE_URL`      | `http://localhost:9099/identitytoolkit.googleapis.com/v1/accounts`      | URL Identity Toolkit                  |
| `FIREBASE_TOKEN_BASE_URL`         | `http://localhost:9099/securetoken.googleapis.com/v1/token`             | URL Secure Token                      |
| `DATABASE_URL`                    | `jdbc:h2:mem:test;DB_CLOSE_DELAY=-1`                                    | JDBC URL базы данных                  |
| `PORT`                            | `8080`                                                                  | Порт сервера                          |
