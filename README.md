# СМЭВ 3 сервис

- Отправка запросов
- TODO Отправка запросов с вложениями mtom
- TODO Отправка запросов с вложениями на FTP
- Отправка Ack подтверждения получения
- Получение ответа(чтение очереди входящих ответов)
- Работа через брокер сообщений(Kafka)
- Поддержка версий схемы 1.1, 1.2 и 1.3 (сейчас нужно поменять версию в пути импортов и в неймспейсе в файле kotlin/com/nineone/smev/smev.kt)
- Поддержка нестандартных имен SOAP сервисов и портов (для региональных СМЭВ)
- TODO Предоставление видов сведений (чтение очереди запросов, подтверждение получения, отправка ответа)
- TODO поддержка переключения версий схемы - задание версии в параметрах
- TODO трансформация и вычисление Digest бизнес-части запроса для последющей подписи ЭП СП на стороне клиента(подписание сообщения ЭП должностного лица)

## Зависимости

- Разархивировать в папку `dist` дистрибутив CryptoPRO JCP2(версия jcp-2.0.40502)
- Загрузить в папку `libs` зависимости СМЭВ Клиента ("Рекомендуемая версия библиотек для сборки клиента СМЭВ 3. Схема версии 1.3 от 23.04.2020" https://smev3.gosuslugi.ru/portal/)

## Сборка Docker образа

`docker build -t registry.91.team/smev/service:latest .`

## Запуск Docker контейнера

```
docker run --rm -it \
  -v "$(pwd)/keys:/var/opt/cprocsp/keys/root" \
  --env-file .env \
  registry.91.team/smev/service:latest
```

## Разработка

Установить КриптоПро JCP2 и зависимости:
- `sudo bash setup_console.sh /%jdk_path% -jre /%jdk_path%/jre`
- `sudo cp dependencies/xmlsec-1.5.0.jar /%jdk_path%/jre/lib/ext/`
- `sudo cp dependencies/commons-logging-1.1.1.jar /%jdk_path%/jre/lib/ext/`

(Полное удаление КриптоПро JCP2):
```
sudo bash setup_console.sh \
    /%jdk_path% \
    -jre /%jdk_path%/jre \
    -force -ru -uninstall -jcp -cpssl -rmsetting
```

Скопировать файлы ключевого контейнера в папку `/var/opt/cprocsp/keys/$USER/smev`

Изменить конфигурацию приложения

Тестирование работы с kafka:
- `docker-compose up`
- `kafkacat -P -b localhost:9092 -t backend2connector -p 0 files/kafka-request.xml`
- http://localhost:9000/topic/connector2backend/messages?partition=0&offset=0&count=100&keyFormat=DEFAULT&format=DEFAULT
## Конфигурация

Конфигурирование осуществляется через переменные окружения. По умолчанию заданы параметры федерального СМЭВ 3.


| Имя | Обязательно | Значение по умолчанию | Описание|
| ----------- | ----------- | ----------- | ----------- |
| SCHEMA_URL | нет |http://172.20.3.12:5000/ws/smev-message-exchange-service-1.3.wsdl | точка доступа СМЭВ 3 |
| KEY_ALIAS | да | - | наименование ключевого контейнера, список доступных можно посмотреть командой `bin/smev aliases` |
| KEY_PASSWORD | да | - | пароль ключевого контейнера |
| NODE_ID | нет | - | идентификатор узла, нужен при работе нескольких узлов одновременно - узел получает ответы только по своему NODE_ID |
| TEST_MESSAGE | нет | false | признак тестового сообщения, по умолчанию `false`, должен быть `true` при работе в тестовом контуре СМЭВ 3 |
| SOAP_SERVICE_NAME | нет | SMEVMessageExchangeService | имя soap сервиса - в региональных СМЭВ имена сервисов и эндпоинтов могут отличаться от схемы федерального СМЭВ |
| SOAP_ENDPOINT_NAME | нет | SMEVMessageExchangeEndpoint | имя soap эндпоинта |
| JCP_LICENSE_KEY | нет | - | Ключ лицензии КриптоПро JCP2(при использовании Docker образа) |
| KAFKA_ADDRESS | нет | localhost:9092 | Адрес Kafka
| KAFKA_REQUSTS_TOPIC | нет | smev_requests | Kafka topic для запросов
| KAFKA_RESPONSES_TOPIC | нет | smev_responses | Kafka topic для ответов
| LOG_LEVEL | нет | ERROR | Уровень логирования

## Использование

- Запуск kafka сервиса `bin/smev server`
- Вывод списка наименований установленных ключевых контейнеров `bin/smev aliases`

