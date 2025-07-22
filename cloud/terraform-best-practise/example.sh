cd terraform/environments/dev
terraform init -backend-config=../../backends/dev-backend.tf
terraform plan -var-file=../../config/dev.tfvars
terraform apply -var-file=../../config/dev.tfvars


#test stateler ilgili environmentlarda kalır
cd terraform-project/environments/dev
terraform init -backend-config=../../config/backend-config-dev.tfvars
terraform plan
terraform apply

#prod stateler ilgili environmentlar altında kalır
cd terraform-project/environments/prod
terraform init -backend-config=../../config/backend-config-prod.tfvars
terraform plan
terraform apply

#spesifik objeleri sileriz.
cd terraform-project/environments/dev
terraform destroy -target=module.kafka

#silinen objeleri tekrar yaratmak için
terraform apply
