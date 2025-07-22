resource "aws_instance" "app" {
  count         = var.instance_count
  ami           = var.ami_id
  instance_type = var.instance_type
  subnet_id     = element(var.subnet_ids, count.index % length(var.subnet_ids))

  tags = {
    Name = "${var.app_name}-${count.index}"
    App  = var.app_name
  }
}
