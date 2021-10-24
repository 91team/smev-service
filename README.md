## Зависимости

- Загрузить в папку `dist` CryptoPRO JCP2(версия jcp-2.0.40502)
- Загрузить в папку `lib` зависимости СМЭВ Клиента ("Рекомендуемая версия библиотек для сборки клиента СМЭВ 3. Схема версии 1.3" https://smev3.gosuslugi.ru/portal/)
- Загрузить в папку `lib` crypto-2.0.jar ("Библиотека crypto-2.0 для создания и проверки ЭП" https://smev3.gosuslugi.ru/portal/)

## Сборка

`docker build -t harbor.91.vpn/smev/adapter:latest .`

`docker push harbor.91.vpn/smev/adapter:latest`

## Запуск

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

Добавить зависимости из папки lib - File > Project Structure > Modules > Dependencies
