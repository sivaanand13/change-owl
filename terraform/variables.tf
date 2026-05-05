variable "tenancy_ocid" { type = string }
variable "compartment_id" { type = string }
variable "region" { default = "us-phoenix-1" }
variable "ssh_public_key" { type = string }

variable "instance_image_ocid" {
  type    = string
  default = "ocid1.image.oc1.phx.aaaaaaaalr6cv36cnbbyf2ghvwyeog4wsilgpcol7ct23ojjwq6t7o2dtqqa"
}
