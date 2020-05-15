# AWS API

Api used to retrieve all ec2 instances for aws region.

## How to run

### 1. Prequisites
- serverless installed
- aws client installed + configured
- java8

### 2. Intelij:
Once imported, just run from main Application.class. App starts on http://localhost:8085/ (port configurable in properties).
If not using intelij run jar (located in lib directory). Aws creds will be pulled in from your local creds and profile file.
You can access the app:
    
    http://localhost:8085/aws-api/v1/regions/eu-west-1/ec2

### 3. Deploy to your aws account:
Deployment is using open source project serverless.com (install it here: https://serverless.com/framework/docs/getting-started/ and set your aws creds)
Build project:  
    
    ./gradlew clean build
    
This will build project in build/distributions/aws-api-1.0-SNAPSHOT.zip that is uploaded to aws (buildZip task)

Once done, in root of this project run:
        
        serverless deploy
    
Cloudformation stack is created, once it finish it'll print uri for api gateway. 

#### IMPORTANT: Please create your user and add it to the group apiEc2UseGroup at this point in order to call api.
Lambda has access to describe ec2 instances with iam policy.

### 4. Api documentation
Once run go to: 

    http://localhost:8085/swagger-ui.html#/
    
This is swagger ui where you can find documentation about api. Currently not possible to call from there as
there is no place to put credentials. There fore please test through postman. There is also swagger.json static file
that can be used as documentation (also can be used to import to api gateway)

### 5. Test (Postman)
Follow: https://aws.amazon.com/premiumsupport/knowledge-center/iam-authentication-api-gateway/ (Send a request to test the authentication settings)
The app is deployed on: 

        https://ehuzqavkg6.execute-api.eu-west-1.amazonaws.com/dev/aws-api/v1/regions/eu-west-1/ec2

Test credentials:  
AccessKey: enquire  
SecretKey: enquire    
Aws Region: eu-west-1  
This user is add to the group: apiEc2UseGroup (from serverless.yaml) and has permission to call this endpoint.

Logs can be found in cloudwatch.

## Why this solution:
1. Providing this api won't be used often or have high throughput it is cheaper to implement as lambda. No admin work,
only using resources it needs at the time, and can be free of charge when not in use.
2. Used Springboot to be able to test and start locally, however lambda integration not tested until deployed. This
could be improved (might be some libs to help with that)
3. Would add some smoke test after deployment to exercise endpoints and make sure all is wired together correctly and
auth set up for user.

 
## Further improvements planned:
1. As per advice https://github.com/awslabs/aws-serverless-java-container/wiki/Quick-start---Spring-Boot, There are a 
number of optimization we can make in our Spring application to minimize cold start times in Lambda. 
2. Find AmazonEc2 client stub to use in component tests (possibly with wiremock). At the moment it is mocked and doesn't 
execute real http request.
3. With new aws sdk we are creating ec2 client for every request. This is not very efficient and should be 
cached per region.
4. Externalise some request validation properties. Also http client properties to further improve performance.
3. Allow testing from swagger ui. Add auth to the ui that takes keys.

## Trableshoot
    
    {
        "Message": "User: arn:aws:iam::*****:user/api-test-user is not authorized to perform: execute-api:Invoke on resource: arn:aws:execute-api:eu-west-1:********1633:ehuzqavkg6/dev/GET/aws-api/v1/regionseu-west-1/ec2"
    }
    
 Please add your user to the apiEc2UseGroup. This might take a while so please be patient and try in few minutes.