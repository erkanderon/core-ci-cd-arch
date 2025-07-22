module "rabbitmq" {
  source          = "../../../modules/compute"
  app_name        = "rabbitmq"
  instance_count  = 3
  instance_type   = "t3.small"
  ami_id          = var.rabbitmq_ami
  subnet_ids      = module.network.private_subnet_ids
}
