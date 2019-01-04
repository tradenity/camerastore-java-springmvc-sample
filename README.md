
Welcome to the CameraStore sample application for Tradenity Java SDK
=================================


[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy)


## Live demo

Here you can find live demo of the [Camera store sample application](http://camera-store-sample.tradenity.com/).
This is the application we are going to build here.


## Prerequisites

(Will install automatically via maven)

-  Spring framework > 4.2 (other versions may work but not tested)
-  [Tradenity Java SDK](https://github.com/tradenity/java-sdk)
-  [Spring extensions for the Java SDK](https://github.com/tradenity/java-sdk-spring-ext)



## Setup your credentials

First of all, you have to get API keys for your store, you can find it in your store `Edit` page.
To get there navigate to the stores list page, click on the `Edit` button next to your store name, scroll down till you find the `API Keys` section.


## Initialize the library

Add your Store keys to `resources/application.properties` file:

`application.properties`

```ruby
tradenity.publicKey=pk_xxxxxxxxxxxxxxxxxxxxxxx
tradenity.secretKey=sk_xxxxxxxxxxxxxxxxxxxxxxx

```


## Running on Heroku

In order to make it easy for you to test run this application, we made it Heroku ready, meaning you can run it as is in the Heroku PaaS.
You just create a Heroku app and add config variables for your store keys

```sh
heroku create
heroku config:set TRADENITY_PUBLIC_KEY=pk_xxxxxxxxxxxxxxxxxxxxxxxxxxx
heroku config:set TRADENITY_SECRET_KEY=sk_xxxxxxxxxxxxxxxxxxxxxxxxxxx
heroku config:set STRIPE_PUBLIC_KEY=pk_xxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```


## Tutorials and sample applications


We also provide a detailed explanation of the code of this sample applications in the form of a step by step tutorials:

[Camera store for spring mvc tutorial](http://docs.tradenity.com/kb/tutorials/java/springmvc).

