# PostgreSQL экспортер и Grafana

В этом проекте теперь используется **PostgreSQL** для хранения данных. Запущен контейнер с PostgreSQL, и настроен экспорт данных с помощью соответствующего экспортера.

Т.к. PostgreSQL и Exporter были запущены в докере (который был запущен в WSL), чтоб экспортет видел постгре, пришлось их объеденить в одну сеть.

Потому что первоначально, вот при таких параметрах запуска, в метриках было написано, что соединение с БД не установленно.
```
docker run -d \
  --name postgres-exporter \
  -p 9187:9187 \
  -e DATA_SOURCE_NAME="postgresql://postgres:123@localhost:5433/bankBD?sslmode=disable" \
  quay.io/prometheuscommunity/postgres-exporter:latest
```

После объедениения в сеть

`docker network create bank-network`

`docker network connect bank-network auth-postgres`

`docker network connect bank-network prometheus`

Был такой запуск
```
docker run -d \
  --name postgres-exporter \
  -p 9187:9187 \
  --network bank-network \
  -e DATA_SOURCE_NAME="postgresql://postgres:123@auth-postgres:5432/bankBD?sslmode=disable" \
  quay.io/prometheuscommunity/postgres-exporter:latest
```

И в метриках уже видно, чтоб БД найдена. Есть контакт!)

В Grafana Labs был найден оффициальный дашборд от экспортера и графаны [(тык)](https://grafana.com/grafana/dashboards/14114-postgres-overview/)

![image](https://github.com/user-attachments/assets/8b32e848-e5fc-4600-8b4b-37b1d5341099)

