Creating and signing keys:
Get Gnupg http://www.gnupg.org/
1) Generate a key: gpg --gen-key
2) Enter a pass phrase to encrypt key
3) Export my public key: gpg --export --armor (can pipe it to an output file)
4) Send public key to other side
5) Receive their public key
6) Import their public key: gpg --allow-non-selfsigned-uid --import (their key)
7) List keys: gpg --list-keys
8) Signed their public key with ours: gpg --sign-key (their pub key id, ie 75299CFA for MS)
9) Now you can encrypt file: gpg -r 75299CFA -e -a test.txt (this encrypts in .asc format)
   To sign and encrypt the file: gpg -se -r 75299CFA -a --passphrase-fd 0 < passphrase.txt(file containing the passphrase)
   
   
To remove keys:
1) gpg --delete-keys UID or sometimes may need to do gpg --delete-secret-keys UID first and then delete-keys next