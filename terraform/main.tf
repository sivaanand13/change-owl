resource "oci_core_vcn" "changeowl_vcn" {
  compartment_id = var.compartment_id
  cidr_block     = "10.0.0.0/16"
  display_name   = "changeowl-vcn"
  dns_label      = "changeowl"
}

resource "oci_core_subnet" "changeowl_subnet" {
  compartment_id = var.compartment_id
  vcn_id         = oci_core_vcn.changeowl_vcn.id
  cidr_block     = "10.0.1.0/24"
  display_name   = "changeowl-public-subnet"
}

data "oci_identity_availability_domains" "ads" {
  compartment_id = var.compartment_id
}

data "oci_core_images" "oracle_linux_arm" {
  compartment_id           = var.compartment_id
  operating_system         = "Oracle Linux"
  operating_system_version = "9"
  shape                    = "VM.Standard.A1.Flex"
}

resource "oci_core_instance" "changeowl_server" {
  availability_domain = data.oci_identity_availability_domains.ads.availability_domains[0].name
  compartment_id      = var.compartment_id
  display_name        = "changeowl-engine-01"
  shape               = "VM.Standard.A1.Flex"

  shape_config {
    ocpus         = 2
    memory_in_gbs = 16
  }

  source_details {
    source_type = "image"
    source_id   = data.oci_core_images.oracle_linux_arm.images[0].id
  }

  create_vnic_details {
    assign_public_ip = true
    subnet_id        = oci_core_subnet.changeowl_subnet.id
  }

  metadata = {
    ssh_authorized_keys = var.ssh_public_key
  }
}