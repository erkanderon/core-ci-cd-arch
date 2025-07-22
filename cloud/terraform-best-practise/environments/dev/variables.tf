variable "env" { default = "dev" }

variable "vpc_cidr" {
  default = "10.0.0.0/16"
}

variable "public_subnet" {
  default = "10.0.1.0/24"
}

variable "private_subnets" {
  default = [
    "10.0.2.0/24",
    "10.0.3.0/24",
    "10.0.4.0/24",
    "10.0.5.0/24"
  ]
}

variable "kafka_ami" {
  default = "ami-0abc1234kafka"
}

variable "rabbitmq_ami" {
  default = "ami-0def5678rabbitmq"
}
