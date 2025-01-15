package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.exception;

/**Клас SpecificException є розширенням стандартного виключення RuntimeException і використовується для створення кастомних винятків з додатковими параметрами:

 Поля:

 customMessage: Користувацьке повідомлення, що описує помилку.
 errorCode: Код помилки, що може бути використаний для класифікації чи інтерпретації винятку.
 Конструктор:

 Ініціалізує екземпляр класу, передаючи користувацьке повідомлення та код помилки.
 Методи:

 getCustomMessage(): Повертає користувацьке повідомлення.
 getErrorCode(): Повертає код помилки.
 Призначення: Клас дозволяє створювати специфічні виключення з додатковими даними, що можуть бути використані для кращої обробки помилок та відлагодження.*/

public class SpecificException extends RuntimeException {
    private final String customMessage;
    private final int errorCode;

    public SpecificException(String customMessage, int errorCode) {
        super(customMessage);
        this.customMessage = customMessage;
        this.errorCode = errorCode;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
