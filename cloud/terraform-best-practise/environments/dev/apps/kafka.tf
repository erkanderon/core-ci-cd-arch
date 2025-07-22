module "kafka" {
  source          = "../../../modules/compute"
  app_name        = "kafka"
  instance_count  = 5
  instance_type   = "t3.medium"
  ami_id          = var.kafka_ami
  subnet_ids      = module.network.private_subnet_ids
}
