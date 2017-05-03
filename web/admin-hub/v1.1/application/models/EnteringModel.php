<?php

if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class EnteringModel extends CI_Model
{

    function __construct()
    {
        parent::__construct();
    }


    public function create($entering){
        
        $error = false;

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);

        $insertQuery = "INSERT INTO item_entering(item_id, units, unit_price, discount, iva, provider_id, entering_date, total, notes) VALUES (?,?,?,?,?,?,NOW(), ?,?)";
        $error = $this->db->query($insertQuery, array($entering->item_id,
            $entering->units,
            $entering->price,
            $entering->discount,
            $entering->iva,
            $entering->provider_id,
            $entering->price * $entering->units,
            $entering->notes));

        $updateQuery = "UPDATE item SET available_units = available_units + :units, modified = NOW() WHERE id = :id";
        $error = $this->db->query($updateQuery, array($entering->units, $entering->item_id));

        return $error;

    }
    
}