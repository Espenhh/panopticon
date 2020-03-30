![panopticon](/logo.png?raw=true)

panopticon is a monitoring system for all your applications that is dead simple to install and use. 

**You are just four simple steps away from using panopticon in production:**

1. Install panopticon on a server / in the cloud
1. Include the panopticon client in your application (or use the API directly)
1. Write your first measurement
1. Your application is now listed in your personal dashboard! 🎉

## Configure AWS

Install `aws`, `eb` and `ansible` command line tools:

```
brew install aws
brew install eb
brew install ansible
```

Configure a AWS profile for panopticon:


`~.aws/credentials`

```
[panopticon]
aws_access_key_id = <ADD YOURS HERE>
aws_secret_access_key = <ADD YOURS HERE>
```

Run `eb init --profile panopticon` in `/backend`

## Deployment

See the "Configure AWS" section first

- `cd frontend && ./deploy.sh`
- `cd backend && ./deploy.sh`


## Running panopticon locally

* [Frontend](/frontend)
* [Backend](/backend)
