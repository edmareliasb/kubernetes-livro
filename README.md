# Back-end Java
Microsserviços, Spring Boot e Kubernetes

A aplicação é composta de três microserviços, a user-api, a product-api e a shopping-api. A user-api possui os serviços para gerenciar os usuários da aplicação. A product-api possui os serviços para gerenciar os produtos disponíveis para compras. A shopping-api os serviços para que usuários realizem compras que por sua vez, interage com os outros dois microserviços.

![image](https://user-images.githubusercontent.com/16382981/119489556-316a5a00-bd32-11eb-9734-521193c5243d.png)

### Ambiente
[IntelliJ IDEA](https://www.jetbrains.com/pt-br/idea/download), [Maven](https://maven.apache.org), [Docker](https://www.docker.com/products/docker-desktop), [pgAdmin](https://www.pgadmin.org/download) e [PostgreSQL](https://www.postgresql.org/download) (se preferir, pode usar a [imagem](https://hub.docker.com/_/postgres) docker do PostgreSQL)

`
docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=mypassword postgres
`

### Bibliotecas
Spring Boot, Spring Web, Spring Data, [Flyway](https://github.com/flyway/flyway) e [PostgreSQL](https://mvnrepository.com/artifact/org.postgresql/postgresql)

### Running
1 - Devido ao compartilhamento das classes do projeto shopping-client entre os demais projetos, é necessário fazer a sua instalação através do Maven, para que o jar do projeto fique disponível no repositório local do Maven na sua máquina.
2 - É necessário configurar o arquivo settings.xml do Maven, para permitir a utilização do plugin do spotify para rodar a aplicação em containeres docker. Normalmente o arquivo em um sistema operacional Windows, fica localizado em C:/Users/Usuario/.m2

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings 
    xmlns="http://maven.apache.org/SETTINGS/1.0.0" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">

    <pluginGroups>
        <pluginGroup>com.spotify</pluginGroup>
    </pluginGroups>
</settings>
```

#### Running with docker
Em cada API, será necessário executar os seguintes comandos:
```cmd
mvn clean install
mvn dockerfile:build
```
Obs: será necessário habilitar a opção "**Expose daemon on tcp://localhost:2375 without TLS**" nas configurações do docker, conforme mostra a imagem abaixo:

![image](https://user-images.githubusercontent.com/16382981/118900145-a2e77a00-b8e6-11eb-9b83-ce5c729e88d7.png)

Feito isso, basta executar o comando a seguir para subir as aplicações:

```
docker-compose up
```

e o comando abaixo para parar as aplicações:

```
docker-compose down
```

#### Kubernetes

Se você ainda não possui o Kubernetes instalado na sua máquina e utiliza o sistema operacional Windows junto com o Docker for Desktop, basta acessar a tela de configuração do Docker for Desktop e ativar a opção "Enable Kubernetes", como mostra a imagem abaixo:

![image](https://user-images.githubusercontent.com/16382981/119264235-ac0f6a00-bbb8-11eb-8db2-e1ab4d95de71.png)


#### Kubectl

Uma das formas de se interagir com o Kubernetes é através de linha de comando. Para isso vamos instalar o Kubectl. No sistema operacional Windows, basta acessar a página oficial do Kubernetes e seguir a [documentação](https://kubernetes.io/docs/tasks/tools/install-kubectl-windows/). Para verificar se deu tudo certo na instalação, ou caso deseja verificar se você já possui o Kubectl instalado, basta digitar o seguinte comando:

```
kubectl version --client
```

#### Kubernetes dashboard

Outra forma de interagir com o Kubernetes é usando o Dashboard, que por padrão não é instalado de forma automática. Para instalar, basta executar o seguinte comando, como é indicado no [projeto oficial](https://github.com/kubernetes/dashboard) disponibilizado no GitHub:

```
kubectl create -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.2.0/aio/deploy/recommended.yaml
```

Agora para acessar o dashboard, precisamos criar um usuário no nosso cluster. Para isso, execute o comando:

```
kubectl create -f create-user.yaml
```

Para acessar o dashboard agora, é necessário executar o comando `kubectl proxy` que cria um proxy para acessar a API do Kubernetes, incluindo o dashboard. Após executar o comando, o dashboard estará disponível para ser acessado na URL: http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/#/login

![image](https://user-images.githubusercontent.com/16382981/119269813-f8b26f80-bbcf-11eb-8731-6a6836eaa206.png)

O acesso ao dashboard pode ser feito de duas maneiras, ou com a criação de um arquivo de configuração, ou com o uso de um token para o usuário. O acesso com o token é mais simples, já que o acesso com o arquivo de configuração também precisa do token. Para criar um token, basta executar o comando abaixo:

```
kubectl -n kube-system describe secret $(kubectl -n kube-system get secret | grep loja-admin | awk '{print $1}')
```

Este comando deve retornar algo como:

```
Name:         loja-admin-token-mcgnb
Namespace:    kube-system
Labels:       <none>
Annotations:  kubernetes.io/service-account.name: loja-admin
              kubernetes.io/service-account.uid: b1eb6a1e-8b8b-4e0b-98d1-416023ab9eaa

Type:  kubernetes.io/service-account-token

Data
====
token:      SEU_TOKEN
ca.crt:     1066 bytes
namespace:  11 bytes
```

O próximo passo agora é subir um cluster com o banco de dados Postgres. Para isso, execute os seguintes comandos:

```
// Subindo o cluster
kubectl create -f postgres-deployment.yaml
kubectl create -f postgres-service.yaml

// Mapeando a porta do SO para a porta do pod permitindo acessar dentro do cluster
kubectl port-forward svc/postgres 5000:5432

// Mapeando as variáveis de ambiente
kubectl create -f config-map.yaml
```

O mesmo precisa ser feito para criar os clusters das nossas APIs:

```
kubectl create -f user-api/deploy/deployment.yaml
kubectl create -f user-api/deploy/service.yaml

kubectl create -f product-api/deploy/deployment.yaml
kubectl create -f product-api/deploy/service.yaml

kubectl create -f shopping-api/deploy/deployment.yaml
kubectl create -f shopping-api/deploy/service.yaml
kubectl create -f shopping-api/deploy/configmap.yaml
```

#### Nginx

O Nginx é um seridor web de código aberto que pode ser usado no Kubernetes. Com ele é possível acessar os serviços no Kubernetes diretamente, sem ter necessidade de abrir uma porta da máquina local para o container. Trata-se de um serviço independente que pode ser instalado no cluster, assim como fizemos com o Postgres e nossas aplicações. Para instalar o Nginx no servidor, execute o seguinte comando:

```
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v0.46.0/deploy/static/provider/cloud/deploy.yaml
```

#### Ingress

O último passo agora é criar um Ingress, que é um elemento do Kubernetes para permitir o acesso externo ao cluster sem a necessidade de executar o comando port-forward. Basicamente o Ingress redireciona um acesso ao cluster para um Service de uma aplicação. Execute o comando a seguir para criar o Ingress:

```
kubectl create -f ingress.yaml

// verificar se o ingress foi criado corretamente
kubectl get ingresses.v1.networking.k8s.io
```

Com o Ingress configurado, agora basta direcionar as requisições do IP mostrado no comando anterior para a URL shopping.com. No Windows, basta editar o arquivo C:\windows\system32\drivers\etc\hosts e adicionar a linha:

```
129.168.99.100 shopping.com
```

Pronto, agora você já pode acessar as APIs usando shopping.com/users, shopping.com/products, etc.
