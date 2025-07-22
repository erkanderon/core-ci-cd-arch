bucket         = "my-terraform-states"
key            = "dev/network/terraform.tfstate"
region         = "eu-central-1"
dynamodb_table = "terraform-locks"
encrypt        = true