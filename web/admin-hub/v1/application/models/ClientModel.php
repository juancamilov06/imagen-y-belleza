<?php


if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class ClientModel extends CI_Model{

    function __construct(){
        parent::__construct();
    }

    public function updateClient($json){

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);

        $updateQuery = "UPDATE client SET code = ?, company = ?, contact = ?, mail_address = ?, address = ?, neighborhood = ?, nit = ?, phone_one = ?, phone_two = ?, phone_three = ?, modified = NOW() WHERE id = ?";
        return $this->db->query($updateQuery, array($json->code,$json->company, $json->contact,
            $json->mail_address,$json->address, $json->neighborhood,
            $json->nit, $json->phone_one,$json->phone_two,$json->phone_three,$json->id));
        
    }

    public function getClientById($id){

        $clientsQuery = "SELECT * FROM client WHERE id = " . $id;
        $query = $this->db->query($clientsQuery);
        $results = (object) $query->row_array();

        return $results;

    }
    
    public function getClients(){

        $statement = "SELECT * FROM client";
        $query = $this->db->query($statement);
        return $query->result_array();
    }

    public function getTypes(){

        $statement = "SELECT * FROM client_type";
        $query = $this->db->query($statement);
        return $query->result_array();
    }

    public function updateType($typeId, $id, $latitude, $longitude){

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);
        
        $statement = "UPDATE client SET client_type_id = " . $typeId . ", latitude = " . $latitude . ", longitude = " . $longitude .", modified = NOW() WHERE id = " . $id;
        return $this->db->query($statement);

    }

    public function create($id, $company, $address, $city, $phone_one, $phone_two, $phone_three, $nit, $mail, $contact, $clientType
        , $neighborhood, $user_id, $latitude, $longitude){

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);

        $date = date('Y-m-d', time());
        $insertQuery = "INSERT INTO client(id, client_type_id, neighborhood, company, address, city_id, phone_one, phone_two, phone_three, nit, mail_address, contact, created, modified, user_id, latitude, longitude) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        return $this->db->query($insertQuery, array(
            $id,
            $clientType,
            $neighborhood,
            $company,
            $address,
            $city,
            $phone_one,
            $phone_two,
            $phone_three,
            $nit,
            $mail,
            $contact,
            $date,
            $date,
            $user_id,
            $latitude,
            $longitude
        ));

    }

    public function createAll($object){

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);

        $date = date('Y-m-d', time());
        $insertQuery = "INSERT INTO client(id, client_type_id, neighborhood, company, address, city_id, phone_one, phone_two, phone_three, nit, mail_address, contact, created, modified, user_id, latitude, longitude) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        $error = false;

        foreach($object as $json) {
            $error = $this->db->query($insertQuery, array(
                $json->id,
                $json->client_type_id,
                $json->neighborhood,
                $json->company,
                $json->address,
                $json->city,
                $json->phone_one,
                $json->phone_two,
                $json->phone_three,
                $json->nit,
                $json->mail,
                $json->contact,
                $date,
                $date,
                $json->user_id,
                $json->latitude,
                $json->longitude
            ));
        }

        return $error;
    }

}