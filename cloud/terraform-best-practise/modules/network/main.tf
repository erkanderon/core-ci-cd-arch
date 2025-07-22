resource "aws_vpc" "main" {
  cidr_block = var.vpc_cidr
  tags = {
    Name = "${var.env}-vpc"
  }
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.main.id
}

resource "aws_subnet" "public" {
  cidr_block = var.public_subnet
  vpc_id     = aws_vpc.main.id
  map_public_ip_on_launch = true
  availability_zone       = "eu-central-1a"
  tags = { Name = "${var.env}-public-subnet" }
}

resource "aws_eip" "nat" {
  vpc = true
}

resource "aws_nat_gateway" "nat" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public.id
}

resource "aws_subnet" "private" {
  count             = length(var.private_subnets)
  cidr_block        = var.private_subnets[count.index]
  vpc_id            = aws_vpc.main.id
  availability_zone = "eu-central-1a"
  tags = {
    Name = "${var.env}-private-subnet-${count.index}"
  }
}
