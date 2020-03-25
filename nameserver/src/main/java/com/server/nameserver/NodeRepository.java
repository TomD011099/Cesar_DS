package com.server.nameserver;

import org.springframework.data.jpa.repository.JpaRepository;

interface NodeRepository extends JpaRepository<Node, Long> {
    
}