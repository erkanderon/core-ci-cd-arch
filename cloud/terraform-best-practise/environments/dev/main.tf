module "network" {
  source           = "../../modules/network"
  vpc_cidr         = var.vpc_cidr
  public_subnet    = var.public_subnet
  private_subnets  = var.private_subnets
  env              = var.env
}