# DiscussionForum
Simple REST API for Discussion Forum

## Requirements
- Installed PostgreSQL - version 42.2.13, or possible necessity to change version in build.sbt
- Scala - version 2.13.1

## Running the app
### Before running the app 
1. In application.conf change database.url "/forum" to your database name. Change user and password to yours.

2. Use this query in your postgres database to create necessary tables:
```sql
CREATE TABLE topic(
	id serial PRIMARY KEY,
	topic VARCHAR NOT NULL,
	created_by VARCHAR NOT NULL,
	created TIMESTAMP without time zone NOT NULL,
	last_response TIMESTAMP without time zone NOT NULL
);
CREATE TABLE post
(
  id serial PRIMARY KEY,
  topic_id integer NOT NULL,
  content VARCHAR,
  username VARCHAR NOT NULL,
  email VARCHAR,
  created TIMESTAMP without time zone NOT NULL,
  secret VARCHAR NOT NULL,
  CONSTRAINT topic_post FOREIGN KEY (topic_id)
      REFERENCES topic (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
```

3. Start the app with:
```sbtshell
$ sbt
> compile
> run
```
When the app is running, you can start sending the requests.

### Requests
#### Topics
##### To get all topics from database with limit from config file, sorted by last_response:
```bash
GET /topics
```
```bash
curl http://localhost:9000/topics
```
```json
[
   {
      "created":"2020-06-08 18:51:06.774",
      "created_by":"c",
      "id":1,
      "last_response":"2020-06-15 18:06:15.639",
      "topic":"a"
   },
   {
      "created":"2020-06-15 18:06:14.293",
      "created_by":"Test username",
      "id":49,
      "last_response":"2020-06-15 18:06:14.293",
      "topic":"Test topic"
   },
   {
      "created":"2020-06-15 17:59:25.977",
      "created_by":"Test username",
      "id":48,
      "last_response":"2020-06-15 17:59:25.977",
      "topic":"Test topic"
   },
  ...
]
```

Possible to provide limit and offset parameters to paginate:
```bash
curl "http://localhost:9000/topics?limit=2&offset=5"
```
```json
[
   {
      "created":"2020-06-15 17:48:43.761",
      "created_by":"Test username",
      "id":45,
      "last_response":"2020-06-15 17:48:43.761",
      "topic":"Test topic"
   },
   {
      "created":"2020-06-15 17:44:09.191",
      "created_by":"Test username",
      "id":44,
      "last_response":"2020-06-15 17:44:09.191",
      "topic":"Test topic"
   }
]
```

##### To get one topic with all posts:
```bash
GET /topics/id
```
```bash
curl http://localhost:9000/topics/1
```
```json
{
   "posts":[
      {
         "content":"CZY DZIALA",
         "created":"2020-06-09 15:07:59.466",
         "email":"PAWEL@WP.pl",
         "id":36,
         "secret":"1aedc23a-0726-445b-aab7-811f80bc48f1",
         "topic_id":1,
         "username":"PAWEL"
      },
      {
         "content":"CZY DZIALA",
         "created":"2020-06-09 15:08:24.177",
         "email":"PAWEL@WP.pl",
         "id":37,
         "secret":"76ee3770-19e5-41bb-bc78-95fff0c0c68b",
         "topic_id":1,
         "username":"PAWEL"
      },
...
      {
        "content":"Test content",
        "created":"2020-06-15 18:06:15.639",
        "email":"test@email.om",
        "id":88,
        "secret":"638e0afb-f3ed-4406-8140-fef8d408d836",
        "topic_id":1,
        "username":"test username"
      }
         ],
    "topic":{
        "created":"2020-06-08 18:51:06.774",
        "created_by":"c",
        "id":1,
        "last_response":"2020-06-15 18:06:15.639",
        "topic":"a"
    }
}
```

##### To paginate topic with posts can provide parameters offset, before and after: 
```bash
curl "http://localhost:9000/topics/1?offset=2&after=0&before=2"
```
```json
{
   "posts":[
      {
         "content":"CZY DZIALA",
         "created":"2020-06-09 15:07:59.466",
         "email":"PAWEL@WP.pl",
         "id":36,
         "secret":"1aedc23a-0726-445b-aab7-811f80bc48f1",
         "topic_id":1,
         "username":"PAWEL"
      },
      {
         "content":"CZY DZIALA",
         "created":"2020-06-09 15:08:24.177",
         "email":"PAWEL@WP.pl",
         "id":37,
         "secret":"76ee3770-19e5-41bb-bc78-95fff0c0c68b",
         "topic_id":1,
         "username":"PAWEL"
      },
      {
         "content":"CZY DZIALA",
         "created":"2020-06-09 15:08:47.925",
         "email":"PAWEL@WP.pl",
         "id":38,
         "secret":"1e9c3c42-4a2b-4d50-9877-655c3e0b21f8",
         "topic_id":1,
         "username":"PAWEL"
      }
   ],
   "topic":{
      "created":"2020-06-08 18:51:06.774",
      "created_by":"c",
      "id":1,
      "last_response":"2020-06-15 18:06:15.639",
      "topic":"a"
   }
}
```

##### To add post to topic:
```bash
POST /topics/id
```
```bash
curl --location --request POST 'localhost:9000/topics/1' \
--header 'Content-Type: application/json' \
--data-raw '{
    "content": "Content",
    "nick": "Username",
    "email": "example@gmail.com"
}'
```
```json
{
    "content": "Content",
    "created": "2020-06-15 20:04:13.319",
    "email": "example@gmail.com",
    "id": 89,
    "secret": "abbfe4dd-e068-4fc9-be70-a104cc379d85",
    "topic_id": 1,
    "username": "Username"
}
```

##### Or to just add new topic with corresponding post:
```bash
POST /topics
```
```bash
curl --location --request POST 'localhost:9000/topics/' \
--header 'Content-Type: application/json' \
--data-raw '{
    "topic": "Example topic",
    "content": "Content",
    "nick": "Username",
    "email": "example@gmail.com"
}'
```
```json
{
    "post": {
        "content": "Content",
        "created": "2020-06-15 20:10:04.341",
        "email": "example@gmail.com",
        "id": 90,
        "secret": "2506f79f-a98a-471b-bd7c-bb83b9e9d9c0",
        "topic_id": 50,
        "username": "Username"
    },
    "topic": {
        "created": "2020-06-15 20:10:04.341",
        "created_by": "Username",
        "id": 50,
        "last_response": "2020-06-15 20:10:04.341",
        "topic": "Example topic"
    }
}
```

#### Posts

##### Possible to get all posts, providing offset and limit if needed:
```bash
GET /posts
```
```bash
curl --location --request GET 'localhost:9000/posts?offset=10&limit=2' \
--header 'Content-Type: application/json' \
--data-raw '{
    "topic": "Example topic",
    "content": "Content",
    "nick": "Username",
    "email": "example@gmail.com"
}'
```
```json
[
    {
        "content": "Test content",
        "created": "2020-06-15 17:27:15.664",
        "email": "test@email.om",
        "id": 74,
        "secret": "30124d13-9c53-4853-9346-6918c03084f4",
        "topic_id": 1,
        "username": "test username"
    },
    {
        "content": "CZY DZIALA",
        "created": "2020-06-09 15:08:24.177",
        "email": "PAWEL@WP.pl",
        "id": 37,
        "secret": "76ee3770-19e5-41bb-bc78-95fff0c0c68b",
        "topic_id": 1,
        "username": "PAWEL"
    }
]
```

##### You can update one post providing its unique secret:
```bash
PUT /posts/secret
```
Using example's secret from earlier:
```bash
curl --location --request PUT 'localhost:9000/posts/76ee3770-19e5-41bb-bc78-95fff0c0c68b' \
--header 'Content-Type: application/json' \
--data-raw '{
    "content": "Update example"
}'
```
```json
{
    "content": "Update example",
    "created": "2020-06-09 15:08:24.177",
    "email": "PAWEL@WP.pl",
    "id": 37,
    "secret": "76ee3770-19e5-41bb-bc78-95fff0c0c68b",
    "topic_id": 1,
    "username": "PAWEL"
}
```

##### Or delete it:
```bash
DELETE /posts/secret
```
```bash
curl --location --request DELETE 'localhost:9000/posts/76ee3770-19e5-41bb-bc78-95fff0c0c68b' \
--header 'Content-Type: application/json' \
--data-raw '{
    "content": "Update example"
}'
```
As response we simply get number of lines deleted:
```json
1
```

## Running tests 
To run unit tests:
```sbt
$ sbt
> test
```

To run integration tests:
```sbt
$ sbt
> IntegrationTest / testOnly
```
