package de.yapp.enigma_test;

import android.util.Log;

import org.spongycastle.bcpg.BCPGOutputStream;
import org.spongycastle.bcpg.HashAlgorithmTags;
import org.spongycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.spongycastle.bcpg.sig.Features;
import org.spongycastle.bcpg.sig.KeyFlags;
import org.spongycastle.crypto.generators.RSAKeyPairGenerator;
import org.spongycastle.crypto.params.RSAKeyGenerationParameters;
import org.spongycastle.openpgp.PGPCompressedData;
import org.spongycastle.openpgp.PGPCompressedDataGenerator;
import org.spongycastle.openpgp.PGPEncryptedData;
import org.spongycastle.openpgp.PGPEncryptedDataGenerator;
import org.spongycastle.openpgp.PGPEncryptedDataList;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPKeyPair;
import org.spongycastle.openpgp.PGPKeyRingGenerator;
import org.spongycastle.openpgp.PGPLiteralData;
import org.spongycastle.openpgp.PGPLiteralDataGenerator;
import org.spongycastle.openpgp.PGPOnePassSignature;
import org.spongycastle.openpgp.PGPOnePassSignatureList;
import org.spongycastle.openpgp.PGPPrivateKey;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPPublicKeyEncryptedData;
import org.spongycastle.openpgp.PGPSecretKey;
import org.spongycastle.openpgp.PGPSecretKeyRing;
import org.spongycastle.openpgp.PGPSignature;
import org.spongycastle.openpgp.PGPSignatureGenerator;
import org.spongycastle.openpgp.PGPSignatureList;
import org.spongycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.spongycastle.openpgp.PGPUtil;
import org.spongycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.spongycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.spongycastle.openpgp.operator.PGPDigestCalculator;
import org.spongycastle.openpgp.operator.bc.BcPBESecretKeyEncryptorBuilder;
import org.spongycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.spongycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.spongycastle.openpgp.operator.bc.BcPGPKeyPair;
import org.spongycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.spongycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;
import org.spongycastle.util.io.Streams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import java.util.Iterator;

public class CryptoPGP
{

    /* VARIABLES */
    long CurrentKeyID;          //ID of the "DefaultKey"
    long CurrentSignKeyID;      //ID of the MasterKey / the Key used for signing messages
    PGPSecretKeyRing skRing;    //Keyring containing the "DefaultKey" and the "MasterKey"

    /* CONSTRUCTORS */
    /*
    * Erstellt automatisch einen neuen KeyRing
    *
    * @param    id  Identifier (z.B. Telefonnummer)
    * @param    pass    Passwort (z.B. DeviceID o.ä.)
    * @param    s2kcount    Anzahl der Iterationen des Hashings (kp was das bringt - sieht aber fancy aus): default is 0xc0
    * @param    keylength   Schlüssellänge (1024,2048,4086)
    *
    * @exception    DISKODISKO!
    *
    */
    // keylength --> user input
    public CryptoPGP (String id, char[] pass, int s2kcount, int keylength) throws PGPException, NoSuchAlgorithmException, NoSuchProviderException
    {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        this.skRing = generatePGPKeyRing(id, pass, s2kcount, keylength);
    }

    //DEFAULT
    public CryptoPGP ()
    {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        this.CurrentKeyID = 0;
        this.CurrentSignKeyID = 0;
        this.skRing = (PGPSecretKeyRing) null;
    }

    /* METHODES */

    /*
    *   Verschlüsseln
    *
    *   @param msg ByteArray of the Message (String.getBytes("UTF-8"))
    *   @param EncAlgorithm PGPEncryptedData.* - some do not work (e.g. CAST5)
    *   @param withIntegrityCheck
    *   @param pubKey PublicKey used for encryption
    *   @param Compression CompressionAlgorithmTags.* - choose the compression algorithm
    * */

    // master key --> sign/verify
    // sub key --> default --> encrypt/descrypt
    public byte[] encrypt (byte[] msg, int EncAlgorithm, boolean withIntegrityCheck, PGPPublicKey pubKey, int Compression) throws IOException, PGPException
    {
        //Convert msg to PGPLiteralData
        PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();

        //Generator for Compression
        PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(Compression);

        //Generator for Encryption
        PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(new JcePGPDataEncryptorBuilder(EncAlgorithm).setWithIntegrityPacket(withIntegrityCheck).setSecureRandom(new SecureRandom()).setProvider("BC"));
        //Insert a "public key encryption method" with a valid public key to the EncryptionGenerator
        encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(pubKey).setProvider("SC")); //BC

		/* MSG -> PGPLiteralData (lOut)
         * literal -> compression (cOut)
		 * compression -> encryption
		 */

        //Final Output - msg -> literal -> compression -> encryption
        OutputStream out = new ByteArrayOutputStream();

        //------------------------------/

        //CompressedOut - ByteArray needed for length
        ByteArrayOutputStream cOut = new ByteArrayOutputStream();

        //Output of the compression should be written to cOut (the compression Generator)
        OutputStream lOut = comData.open(cOut);

        //Output of the message (the message itsself) should be written to lOut (the literal Generator)
        OutputStream mOut = lData.open(lOut, PGPLiteralData.BINARY, PGPLiteralData.CONSOLE, msg.length, new Date());

        //write the message
        mOut.write(msg);

        mOut.close();

        lOut.close();

        cOut.close();
        //-----------------------------
        //Output of the compression (cOut) should be written to eOut (the encryption Generator)
        OutputStream eOut = encGen.open(out, cOut.toByteArray().length);

        //write the compressed data
        eOut.write(cOut.toByteArray());

        eOut.close();

        out.close();

        return ((ByteArrayOutputStream) out).toByteArray();

        //return (byte[])null;
    }

    /*
    *   Verschlüsseln
    *
    *   @param msg MessageString
    *   @param EncAlgorithm PGPEncryptedData.* - some do not work (e.g. CAST5)
    *   @param withIntegrityCheck
    *   @param pubKey PublicKey used for encryption
    *   @param Compression CompressionAlgorithmTags.* - choose the compression algorithm
    * */
    public byte[] encrypt (String msg, int EncAlgorithm, boolean withIntegrityCheck, PGPPublicKey pubKey, int Compression) throws IOException, PGPException
    {
        return this.encrypt(msg.getBytes(), EncAlgorithm, withIntegrityCheck, pubKey, Compression);
    }

    /*
    *   Entschlüsseln
    *   This method searches the KeyRing for a valid decryption key
    *
    *   @param encryptedMsg ByteArray containing the encrypted message
    *   @param pass CharArray of the passphrase
    *
    * */
    public byte[] decrypt (byte[] encryptedMsg, char[] pass) throws IOException, PGPException
    {
        return this.decrypt(encryptedMsg, pass, null);
    }

    /*
    *   Entschlüsseln
    *
    *   @param encryptedMsg ByteArray containing the encrypted message
    *   @param pass CharArray of the passphrase
    *   @param KeyIn Key used for decryption
    *
    * */
    public byte[] decrypt (byte[] encryptedMsg, char[] pass, PGPSecretKey KeyIn) throws IOException, PGPException
    {
        InputStream in = PGPUtil.getDecoderStream(new ByteArrayInputStream(encryptedMsg));

        OutputStream out = new ByteArrayOutputStream();

        JcaPGPObjectFactory pgpF = new JcaPGPObjectFactory(in);
        PGPEncryptedDataList enc;

        Object o = pgpF.nextObject();
        //
        // the first object might be a PGP marker packet.
        //
        if (o instanceof PGPEncryptedDataList)
        {
            enc = (PGPEncryptedDataList) o;
        }
        else
        {
            enc = (PGPEncryptedDataList) pgpF.nextObject();
        }

        PGPPrivateKey sKey = null;

        PGPPublicKeyEncryptedData pbe = null;

        Iterator it = enc.getEncryptedDataObjects();

        while (sKey == null && it.hasNext())
        {
            pbe = (PGPPublicKeyEncryptedData) it.next();

             /*
             *
        	 * Check if the KeyID equals the CurrentKeyID - if not, the user should be notified, that the msg has been encrypted
        	 * with a old / no longer "secure" PublicKey
        	 * */
            if (KeyIn == null)
            {
                sKey = (this.skRing.getSecretKey(pbe.getKeyID()) == null) ? null : this.skRing.getSecretKey(pbe.getKeyID()).extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("SC").build(pass));
            }
            else
            {
                sKey = KeyIn.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("SC").build(pass));
            }
        }
        if (sKey == null)
        {
            throw new IllegalArgumentException("secret key for message not found.");
        }

        InputStream clear = pbe.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("SC").build(sKey));

        JcaPGPObjectFactory plainFact = new JcaPGPObjectFactory(clear);

        Object message = plainFact.nextObject();

        if (message instanceof PGPCompressedData)
        {
            PGPCompressedData cData = (PGPCompressedData) message;
            JcaPGPObjectFactory pgpFact = new JcaPGPObjectFactory(cData.getDataStream());

            message = pgpFact.nextObject();
        }

        if (message instanceof PGPLiteralData)
        {
            PGPLiteralData ld = (PGPLiteralData) message;

            InputStream unc = ld.getInputStream();

            Streams.pipeAll(unc, out);

            out.close();
        }
        else if (message instanceof PGPOnePassSignatureList)
        {
            throw new PGPException("encrypted message contains a signed message - not literal data.");
        }
        else
        {
            throw new PGPException("message is not a simple encrypted file - type unknown.");
        }

        if (pbe.isIntegrityProtected())
        {
            if (!pbe.verify())
            {
                Log.d("IntegrityCheck", "message failed integrity check");
            }
            else
            {
                Log.d("IntegrityCheck", "message integrity check passed");
            }
        }
        else
        {
            Log.d("IntegrityCheck", "no message integrity check");
        }
        //return ((ByteArrayOutputStream)out).toString("UTF-8");
        return ((ByteArrayOutputStream) out).toByteArray();
    }

    private PGPSecretKeyRing generatePGPKeyRing (String id, char[] pass, int s2kcount, int keylength) throws PGPException, NoSuchAlgorithmException, NoSuchProviderException
    {
        PGPSecretKeyRing skeyring = generatePGPKeyRingGenerator(id, pass, s2kcount, keylength).generateSecretKeyRing();

        return skeyring;
    }

    private PGPKeyRingGenerator generatePGPKeyRingGenerator (String id, char[] pass, int s2kcount, int keylength) throws PGPException, NoSuchAlgorithmException, NoSuchProviderException
    {

        RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();

        kpg.init(new RSAKeyGenerationParameters(BigInteger.valueOf(0x10001), new SecureRandom(), keylength, 12));

        PGPKeyPair rsakp_sign = new BcPGPKeyPair(PGPPublicKey.RSA_SIGN, kpg.generateKeyPair(), new Date());

        PGPKeyPair rsakp_enc = new BcPGPKeyPair(PGPPublicKey.RSA_ENCRYPT, kpg.generateKeyPair(), new Date());
        /*
        PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1);

		PGPKeyRingGenerator keyRingGen = new PGPKeyRingGenerator(
				PGPSignature.NO_CERTIFICATION,
				rsakp_sign,
				id,
				sha1Calc,
				null,
				null,
				new JcaPGPContentSignerBuilder(rsakp_sign.getPublicKey().getAlgorithm(),HashAlgorithmTags.SHA1),
				new JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256, sha1Calc).setProvider("SC").build(pass)
				);

		keyRingGen.addSubKey(rsakp_enc);
		this.CurrentKeyID = rsakp_enc.getKeyID();
		this.CurrentSignKeyID = rsakp_sign.getKeyID();

		return keyRingGen;
		*/

        PGPSignatureSubpacketGenerator signhashgen = new PGPSignatureSubpacketGenerator();

        signhashgen.setKeyFlags(false, KeyFlags.SIGN_DATA | KeyFlags.CERTIFY_OTHER);

        signhashgen.setPreferredSymmetricAlgorithms
                (false, new int[]{
                        SymmetricKeyAlgorithmTags.AES_256,
                        SymmetricKeyAlgorithmTags.AES_192,
                        SymmetricKeyAlgorithmTags.AES_128
                });

        signhashgen.setPreferredHashAlgorithms
                (false, new int[]{
                        HashAlgorithmTags.SHA256,
                        HashAlgorithmTags.SHA1,
                        HashAlgorithmTags.SHA384,
                        HashAlgorithmTags.SHA512,
                        HashAlgorithmTags.SHA224,
                });

        signhashgen.setFeature
                (false, Features.FEATURE_MODIFICATION_DETECTION);

        // Create a signature on the encryption subkey.
        PGPSignatureSubpacketGenerator enchashgen =
                new PGPSignatureSubpacketGenerator();
        // Add metadata to declare its purpose
        enchashgen.setKeyFlags
                (false, KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE);

        // Objects used to encrypt the secret key.
        PGPDigestCalculator sha1Calc =
                new BcPGPDigestCalculatorProvider()
                        .get(HashAlgorithmTags.SHA1);
        PGPDigestCalculator sha256Calc =
                new BcPGPDigestCalculatorProvider()
                        .get(HashAlgorithmTags.SHA256);

        PBESecretKeyEncryptor pske =
                (new BcPBESecretKeyEncryptorBuilder
                        (PGPEncryptedData.AES_128, sha256Calc, s2kcount)) //AES_256 <---- 256 ist zu lang!!!
                        .build(pass);

        PGPKeyRingGenerator keyRingGen =
                new PGPKeyRingGenerator(
                        s2kcount,
                        rsakp_sign,
                        id,
                        sha1Calc,
                        signhashgen.generate(),
                        null,
                        new BcPGPContentSignerBuilder(rsakp_sign.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1),
                        pske);

        keyRingGen.addSubKey(rsakp_enc, enchashgen.generate(), null);

        this.CurrentKeyID = rsakp_enc.getKeyID();

        //SIGNING SUBKEY

        PGPKeyPair rsakp_subsign = new BcPGPKeyPair(PGPPublicKey.RSA_SIGN, kpg.generateKeyPair(), new Date());
        PGPSignatureSubpacketGenerator subsignhashgen = new PGPSignatureSubpacketGenerator();

        subsignhashgen.setKeyFlags(false, KeyFlags.SIGN_DATA);

        keyRingGen.addSubKey(rsakp_subsign, subsignhashgen.generate(), null);

        this.CurrentSignKeyID = rsakp_subsign.getKeyID();

        return keyRingGen;

    }

    /*
    *   Adds a new encryption key to the keyring
    *
    *   @param id Identifier - must be identical to the id of the keyring
    *   @param pass - must be identical to the passphrase of the keyring
    *   @param s2kcount used for hasing - simply use 0xc0
    *   @param keylength - use something like 1024, 2048 - take care: possible that everything above 4096 does not work!
    * */
    // ~ 20 sec
    public PGPSecretKeyRing addNewEncryptionKey (String id, char[] pass, int s2kcount, int keylength) throws PGPException, NoSuchAlgorithmException, NoSuchProviderException
    {
        PGPKeyRingGenerator krg = generatePGPKeyRingGenerator(id, pass, s2kcount, keylength);

        RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();

        kpg.init(new RSAKeyGenerationParameters(BigInteger.valueOf(0x10001), new SecureRandom(), keylength, 12));

        PGPKeyPair rsakp_enc = new BcPGPKeyPair(PGPPublicKey.RSA_ENCRYPT, kpg.generateKeyPair(), new Date());

        PGPSignatureSubpacketGenerator enchashgen =
                new PGPSignatureSubpacketGenerator();
        // Add metadata to declare its purpose
        enchashgen.setKeyFlags
                (false, KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE);

        krg.addSubKey(rsakp_enc, enchashgen.generate(), null);

        this.CurrentKeyID = rsakp_enc.getKeyID();
        //generate KeyRingGenerator
        //add new encryption key
        //fuck the rest
        return krg.generateSecretKeyRing();
    }

    public PGPPublicKey getCurrentPublicKey () throws Exception
    {
        if (skRing == null)
        {
            throw new Exception("No key available - did you generate / load the KeyRing?");
        }

        PGPPublicKey pubKey = skRing.getPublicKey(CurrentKeyID);

        if (pubKey == null)
        {
            throw new Exception("No key available - wrong KeyID?");
        }

        return pubKey;

    }

    // use for SharedPreferences
    public void ImportKeyRing (byte[] KeyRing) throws IOException, PGPException
    {
        this.skRing = new PGPSecretKeyRing(KeyRing, new JcaKeyFingerprintCalculator());
    }

    /*
    *   Used for signing messages
    *   Order is: Msg -> sign -> encrypt -> decrypt -> verify
    *
    *   @param toSign - ByteArray of the message to sign
    *   @param signKey - Key used for signing - CAUTION: Key MUST BE marked as "SigningKey" (use the MasterKey / CurrentSignKeyID)
    *   @param pass - Passphrase of the signing key
    * */
    // passphrase --> deviceid
    public byte[] sign (byte[] toSign, PGPSecretKey signKey, char[] pass) throws PGPException, IOException
    {
        PGPPrivateKey privKey;
        privKey = signKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("SC").build(pass));
        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(signKey.getPublicKey().getAlgorithm(), PGPUtil.SHA1).setProvider("SC"));

        if (!signKey.isSigningKey())
        {
            throw new PGPException("Key is not a signing key!");
        }

        signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, privKey);

        Iterator it = signKey.getPublicKey().getUserIDs();
        if (it.hasNext())
        {
            PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();

            spGen.setSignerUserID(false, (String) it.next());
            signatureGenerator.setHashedSubpackets(spGen.generate());
        }

        OutputStream out = new ByteArrayOutputStream();

        PGPCompressedDataGenerator compressedDataGenerator = new PGPCompressedDataGenerator(PGPCompressedData.UNCOMPRESSED);

        BCPGOutputStream bcpgOutputStream = new BCPGOutputStream(compressedDataGenerator.open(out));

        signatureGenerator.generateOnePassVersion(false).encode(bcpgOutputStream);

        PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();

        OutputStream literalDataGenOutputStream = literalDataGenerator.open(bcpgOutputStream, PGPLiteralDataGenerator.BINARY, PGPLiteralDataGenerator.CONSOLE, toSign.length, new Date());

        InputStream in = new ByteArrayInputStream(toSign);

        int ch;
        while ((ch = in.read()) >= 0)
        {
            literalDataGenOutputStream.write(ch);
            signatureGenerator.update((byte) ch);
        }

        literalDataGenerator.close();
        in.close();

        signatureGenerator.generate().encode(bcpgOutputStream);
        compressedDataGenerator.close();
        bcpgOutputStream.close();
        out.close();

        return ((ByteArrayOutputStream) out).toByteArray();
    }

    /*
    *   Used for verifying messages
    *   Order is: Msg -> sign -> encrypt -> decrypt -> verify
    *
    *   @param toVerify - ByteArray of the message to verify
    *   @param verifyKey - Key used for verifying - CAUTION: Key MUST BE marked as "SigningKey" (use the PublicKey of the MasterKey / CurrentSignKeyID)
    * */
    public byte[] verify (byte[] toVerify, PGPPublicKey verifyKey) throws PGPException, IOException
    {
        InputStream in = PGPUtil.getDecoderStream(new ByteArrayInputStream(toVerify));

        //PGPObjectFactory pgpObjFactory = new PGPObjectFactory(in);
        // TEST - instead of the depricated thing above -
        JcaPGPObjectFactory pgpObjFactory = new JcaPGPObjectFactory(in);
        PGPCompressedData compressedData = (PGPCompressedData) pgpObjFactory.nextObject();

        //Get the signature from the file

        //pgpObjFactory = new PGPObjectFactory(compressedData.getDataStream());
        //TEST - instead of the depricated thing above -
        pgpObjFactory = new JcaPGPObjectFactory(compressedData.getDataStream());
        PGPOnePassSignatureList onePassSignatureList = (PGPOnePassSignatureList) pgpObjFactory.nextObject();
        PGPOnePassSignature onePassSignature = onePassSignatureList.get(0);

        //Get the literal data from the file

        PGPLiteralData pgpLiteralData = (PGPLiteralData) pgpObjFactory.nextObject();
        InputStream literalDataStream = pgpLiteralData.getInputStream();
/*
        InputStream keyIn = new FileInputStream(publicKeyFile);
        PGPPublicKeyRingCollection pgpRing = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(keyIn));
        PGPPublicKey key = pgpRing.getPublicKey(onePassSignature.getKeyID());
*/
        if (verifyKey.getKeyID() != onePassSignature.getKeyID())
        {
            return (byte[]) null; //ACHTUNG: Key nicht vorhanden!!
        }
        //HERERERERERERERERER
        OutputStream literalDataOutputStream = new ByteArrayOutputStream();
        onePassSignature.init(new JcaPGPContentVerifierBuilderProvider().setProvider("SC"), verifyKey);

        int ch;
        while ((ch = literalDataStream.read()) >= 0)
        {
            onePassSignature.update((byte) ch);
            literalDataOutputStream.write(ch);
        }

        literalDataOutputStream.close();

        //Get the signature from the written out file

        PGPSignatureList p3 = (PGPSignatureList) pgpObjFactory.nextObject();
        PGPSignature signature = p3.get(0);

        //Verify the two signatures

        if (onePassSignature.verify(signature))
        {
            return ((ByteArrayOutputStream) literalDataOutputStream).toByteArray();
        }
        else
        {
            return (byte[]) null;
        }
    }

    public byte[] ExportKeyRing () throws IOException
    {
        return skRing.getEncoded();
    }

    /*
    *   Generate a PGPSecretKey with NO RELATION to the KeyRing (can be used as a "one time keypair" for a new private Chat)
    *
    *   @param id - Id
    *   @param pass - CharArray of the passphrase
    *   @param keylength - keylength
    * */
    public PGPSecretKey generatePGPKeyPair (String id, char[] pass, int keylength) throws NoSuchProviderException, NoSuchAlgorithmException, PGPException
    {
/*
        RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();

        kpg.init(new RSAKeyGenerationParameters(BigInteger.valueOf(0x10001), new SecureRandom(), keylength, 12 ));

        PGPKeyPair kp = new JcaPGPKeyPair(PGPPublicKey.RSA_ENCRYPT, kpg.generateKeyPair(), new Date());

        PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1);

        PGPSecretKey key = new PGPSecretKey(PGPSignature.DEFAULT_CERTIFICATION, kp, id, sha1Calc, null, null, new JcaPGPContentSignerBuilder(kp.getPublicKey().getAlgorithm(),HashAlgorithmTags.SHA1), new JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_128).setProvider("SC").build(pass));

        return key;
        */
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "SC");
        kpg.initialize(keylength, new SecureRandom());
        KeyPair pair = kpg.generateKeyPair();

        PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1);
        PGPKeyPair keyPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, pair, new Date());
        PGPSecretKey secretKey = new PGPSecretKey(PGPSignature.DEFAULT_CERTIFICATION, keyPair, id, sha1Calc, null, null, new JcaPGPContentSignerBuilder(keyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1), new JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_128, sha1Calc).setProvider("SC").build(pass));

        return secretKey;
    }

    public long getCurrentKeyID ()
    {
        return this.CurrentKeyID;
    }

    public void setCurrentKeyID (long ID)
    {
        this.CurrentKeyID = ID;
    }

    public long getCurrentSignKeyID ()
    {
        return this.CurrentSignKeyID;
    }

    public void setCurrentSignKeyID (long ID)
    {
        this.CurrentSignKeyID = ID;
    }
}