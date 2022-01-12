package com.code.furry_takeout.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.code.furry_takeout.entity.AddressBook;
import com.code.furry_takeout.mapper.AddressBookMapper;

import com.code.furry_takeout.service.AddressBookService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

}
