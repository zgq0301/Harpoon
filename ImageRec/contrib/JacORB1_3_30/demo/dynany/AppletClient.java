package demo.dynany;

import java.io.*;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TCKind;
import org.omg.DynamicAny.*;
import org.omg.CosNaming.*;


public class AppletClient 
    extends java.applet.Applet 
{
    public static AnyServer s = null;

    public void init()
    {
	try
	{
	    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(this, null);
	    DynAnyFactory dynFactory = DynAnyFactoryHelper.narrow( 
                                           orb.resolve_initial_references("DynAnyFactory"));

	    NamingContextExt nc = 
		NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
	    
	    AnyServer s = AnyServerHelper.narrow(nc.resolve(nc.to_name("DynAnyServer.example")));

	    // char

	    DynAny dyn_any = dynFactory.create_dyn_any_from_type_code(orb.get_primitive_tc( TCKind.tk_char ));
	    dyn_any.insert_char( 'c' );
	    System.out.println("Passing a char..." +  s.generic( dyn_any.to_any() ));

	    // enum

	    TypeCode enum_tc = orb.create_enum_tc("IDL:colors:1.0","colors",
                                   new String[]{"red","green","blue","mauve","magenta","salmon"});
	    DynEnum dyn_enum = (DynEnum)dynFactory.create_dyn_any_from_type_code( enum_tc );
	    dyn_enum.set_as_string("salmon");
	    System.out.println("Passing an enum value..." + s.generic( dyn_enum.to_any() ) );

	    // sequence

	    TypeCode seq_tc = orb.create_sequence_tc( 2, orb.create_string_tc(0));
	    DynSequence dyn_seq = (DynSequence)dynFactory.create_dyn_any_from_type_code( seq_tc );

	    Any [] string_sequence = new Any[2];
	    string_sequence[0] = orb.create_any();
	    string_sequence[1] = orb.create_any();
	    string_sequence[0].insert_string("hello");
	    string_sequence[1].insert_string("world");
	    dyn_seq.set_elements( string_sequence );

	    System.out.println("Passing a sequence of length " + 
			     dyn_seq.component_count() + "..." + s.generic( dyn_seq.to_any() ) );
	    dyn_seq.destroy();

	    // array

	    TypeCode array_tc = orb.create_array_tc( 3, orb.create_string_tc(0));
	    DynArray dyn_array = (DynArray)dynFactory.create_dyn_any_from_type_code( array_tc );

	    string_sequence = new Any[3];
	    string_sequence[0] = orb.create_any();
	    string_sequence[1] = orb.create_any();
	    string_sequence[2] = orb.create_any();
	    string_sequence[0].insert_string("hello");
	    string_sequence[1].insert_string("another");
	    string_sequence[2].insert_string("world");
	    dyn_array.set_elements( string_sequence );

	    System.out.println("Passing an array of length " + dyn_array.component_count() + 
			     "..." +  s.generic( dyn_array.to_any() ) );
	    dyn_array.destroy();

	    // struct

	    /*
	      the hardest part is coming up with a correct call to creat the struct's
	      TypeCode - this is normally statically generated into the helper class 
	    */	   

	    TypeCode struct_tc = orb.create_struct_tc("IDL:Node:1.0", "Node",
			      new org.omg.CORBA.StructMember[]
			      {
				  new org.omg.CORBA.StructMember("name",
					   org.omg.CORBA.ORB.init().create_string_tc(0),null),
				  new org.omg.CORBA.StructMember("next",
					   org.omg.CORBA.ORB.init().create_recursive_sequence_tc(0,1),null)
			      });

	    DynStruct dyn_struct = (DynStruct)dynFactory.create_dyn_any_from_type_code( struct_tc );

	    // create a list of name value pairs that hold the struct members
	    // this struct has two members, so we have 2 array elements
	    
	    // the first struct member is a string, the member name is "name"
	    NameValuePair[]  nvp_seq = new NameValuePair[2]; 
	    Any first_member_any = orb.create_any();
	    first_member_any.insert_string("head");
	    nvp_seq[0] = new NameValuePair("name", first_member_any );

	    /*
	     the second member is a recursive sequence, ie. the element type of
	     the sequence is that of the enclosing  struct, the member name is
	     "next". We'll leave that sequence empty, but we have to provide an
	     initialized any for the empty sequence, so we create a DynAny
	     that initializes an any as an empty sequence
	    */

	    DynSequence second_member = (DynSequence)dynFactory.create_dyn_any_from_type_code(
						      orb.create_sequence_tc( 0, struct_tc ));
	    nvp_seq[1] = new NameValuePair("next", second_member.to_any() );

	    // insert the nvp list into the DynStruct
	    dyn_struct.set_members( nvp_seq );

	    System.out.println("Passing a struct..." + s.generic( dyn_struct.to_any() ) );
	    dyn_struct.destroy();
	    second_member.destroy();

	    // union

	    // setting up TypeCodes gets even more complicated, but you asked for it...

	    org.omg.CORBA.UnionMember[] members = new org.omg.CORBA.UnionMember[3];

	    org.omg.CORBA.Any label_any;
	    label_any = orb.create_any();
	    label_any.insert_octet((byte)0);
	    members[0] = new org.omg.CORBA.UnionMember("s",label_any,
                           org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(2)),null);

	    label_any = org.omg.CORBA.ORB.init().create_any();
	    label_any.insert_char('l');
	    members[1] = new org.omg.CORBA.UnionMember("l",label_any,
                           org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(3)),null);

	    label_any = org.omg.CORBA.ORB.init().create_any();
	    label_any.insert_char('f');
	    members[2] = new org.omg.CORBA.UnionMember("f",label_any,
                           org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(6)),null);

	    TypeCode union_tc = org.omg.CORBA.ORB.init().create_union_tc("IDL:Nums:1.0","Nums",
                            org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(9)), 
			    members);

	    DynUnion dyn_union = (DynUnion)dynFactory.create_dyn_any_from_type_code( union_tc );       

	    Any discriminator_any = orb.create_any();
	    discriminator_any.insert_char('l');
	    DynAny discriminator = dynFactory.create_dyn_any(discriminator_any);

	    dyn_union.set_discriminator( discriminator );
	    dyn_union.member().insert_long(4711);
  	    System.out.println("Passing a union..." + s.generic( dyn_union.to_any()));

	    // setting a different untion value

	    dyn_union.set_to_default_member();
	    dyn_union.member().insert_short((short)19);
  	    System.out.println("Passing the  union again..." + s.generic( dyn_union.to_any()));

	    orb.shutdown(true);
  	    System.out.println("Finished...");

	} 
	catch ( Exception e)
	{
	    e.printStackTrace();
	}
    }
}


