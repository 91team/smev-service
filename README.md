## Зависимости

- Загрузить в папку `dist` CryptoPRO JCP2(версия jcp-2.0.40502)
- Загрузить в папку `libs` зависимости СМЭВ Клиента ("Рекомендуемая версия библиотек для сборки клиента СМЭВ 3. Схема версии 1.3" https://smev3.gosuslugi.ru/portal/)
- Загрузить в папку `libs` crypto-2.0.jar ("Библиотека crypto-2.0 для создания и проверки ЭП" https://smev3.gosuslugi.ru/portal/)

## Сборка образа

`docker build -t harbor.91.vpn/smev/adapter:latest .`

`docker push harbor.91.vpn/smev/adapter:latest`

## Запуск контейнера

```
docker run --rm -it \
  -v "$(pwd)/keys:/var/opt/cprocsp/keys/root" \
  harbor.91.vpn/smev/adapter:latest
```

## Разработка

Установить CryptoPRO JCP2 и зависимости:
- `sudo bash setup_console.sh /%jdk_path% -jre /%jdk_path%/jre`
- `sudo cp dependencies/xmlsec-1.5.0.jar /%jdk_path%/jre/lib/ext/`
- `sudo cp dependencies/commons-logging-1.1.1.jar /%jdk_path%/jre/lib/ext/`

Скопировать файлы ключевого контейнера в папку `/var/opt/cprocsp/keys/$USER/smev`

Изменить конфигурацию приложения

### Форвардинг адреса продакшен контура с удаленного сервера

`ssh -N -f -L 172.20.3.12:5000:172.20.3.12:5000 root@172.23.20.35`

`sudo ifconfig lo0 alias 172.20.3.12`

## Конфигурация

TODO

## Функциональность
- Поддержка версии схемы 1.3
- Отправка запросов
- TODO Отправка запросов с вложениями
- TODO Отправка запросов с вложениями на FTP
- Отправка Ack подтверждения получения
- Получение ответа(чтение очереди входящих ответов)
- TODO предоставление видов сведений
- TODO работа через брокер сообщений(Kafka)
- TODO Внешний REST сервис для отправки запросов и получения ответов