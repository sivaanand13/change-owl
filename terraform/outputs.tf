output "instance_public_ip" {
  description = "Public IP address of the ChangeOwl engine"
  value       = oci_core_instance.changeowl_server.public_ip
}