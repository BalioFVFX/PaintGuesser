# Paint Guesser

![Preview](https://github.com/BalioFVFX/PaintGuesser/blob/main/media/preview.gif?raw=true)

## Introduction

Paint Guesser е игра за която са необходими два играча:

- Художник, който ще рисува
- Гадател, който трябва да отгатне какво е нарисувано.

## Gameplay

Различава се според ролята на играча

- Host (художник)
    - Създава стая в която трябва да се присъедини гадател (чрез IP)
    - След като се присъедини гадателя, за кратък период от време художника вижда изображение, което трябва да запомни и да нарусва.
    - За период от една минута художника трябва да нарисува изображението което е видял.
    - Работата на художника се оценява от гадателя.
- Client (гадател)
    - Присъединява се към стаята създадена от художника.
    - Наблюдава какво рисува художника в реално време.
    - Опитва се да отгатне нарисуваното от художника.
    - Оценява работата на художника, като по време на оценката гадателя вижда и самото изображение, което художника е трябвало да запомни и нарисува.

##    
[Видео](https://youtu.be/3POjvP_UuSo)
##

## Потребителски интерфейс

- Начална страница с навигация до:
    - Лоби - Мястото където се създава или присъединява към игрална стая
    - Игрална стая - Мястото където художника рисува, а гадателя наблюдава. Художника може да избира различни цветове за рисуване и поетапно да изтрива нарисуваното в случай на грешка. И двата играча виждат времето което остава до края на играта на екрана, както и на нотификация, която е прикрепена към Foreground Service.
    - История - Лист съдържащ информация за изиграните игри (Дата, рейтинг и др.). Нарисуваната картина от художника се визуализира при избиране на елемент от листа.
    - Практика - Място подобно на игралната стая, но без таймер и втори играч.

## Стартиране на проекта

### Backend

Преди да започне играта, приложението се нуждае от API от който да взима картинките за рисуване. С играта беше разработен и проект на Spring, който да генерира картинки за рисуване.

⚠️ Spring апликацията трябва да работи на адрес и порт: `http://192.168.0.101:8080`. (Може да бъде променено в `GameConstants.java`. Също така Spring апликацията връща JSON обект, който съдържа линк към генерираната картинка `http://192.168.0.101:8080/images/***`, който може да бъде променен в `IndexController.java`.

### Emulators

Необходими са два броя емулатора. По подразбиране емулаторите се стартират на портове `5554` и `5556` . За да бъде възможна комуникацията между двата емулатора е небходимо да се изпълнят следната команди:

```bash
telnet localhost 5554
```

След което излиза съобщение подобно на това:

```bash
Trying ::1...
Connected to localhost.
Escape character is '^]'.
Android Console: Authentication required
Android Console: type 'auth <auth_token>' to authenticate
Android Console: you can find your <auth_token> in 
'/Users/{username}/.emulator_console_auth_token'
```

Необходимо е да се копира текста от споменатия файл, а след това да се продължи с:

```bash
auth {COPIED TEXT}
```

След успешното изпълнение на командата auth е необходимо да пренасочим портовете:

```bash
redir add tcp:6000:4000
```

Повече информация: [Тук](https://developer.android.com/studio/run/emulator-networking) и [тук](https://stackoverflow.com/questions/4278037/communication-between-two-android-emulators).

Процеса за пренасочването на портовете може да бъде автоматизиран със създаването на executable файл със следното съдържание:

```bash
(sleep 1; echo "auth {{AUTH_KEY_FROM_emulator_console_auth_token}}"; sleep 1; echo "redir add tcp:6000:4000"; sleep 1) | telnet localhost 5554
```

След това е необходимо емулатора с порт `5554` да създаде играта, а емулатора с порт `5556` да се присъедини към играта използвайки `10.0.2.2` за IP адрес.

---

## About

Играта е създадена като проект за **CSCB763 Разработване на мобилни приложения.**

## Изисквания

Използването на външни библиотеки в приложението НЕ е разрешено, освен ако тяхното имплементиране е обосновано и защитено от студента предварително!

> Изискванията към разработеното приложение са:
> 
> - да съдържа поне два компонента Activity, комуникиращи помежду си със съобщение Intent;
> - да са използвани фрагменти;
> - да се свързва с интернет, извлича информация от API и правилно да показва тази информация на екран на мобилно устройство;
> - да съдържа поне една услуга (Service);
> - да използва работна нишка, изпълнявана паралелно с основната;
> - да съхранява и извлича данни в БД SQLite.
> - applicationId (package name) трябва да включва факултетния номер
