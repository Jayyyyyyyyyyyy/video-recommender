#!/usr/bin/env bash
# mvn clean package -U

# hosts=(55 56 293 296 297)
# hosts=(56 293)
# hosts=(55)

 hosts=(296 297)

#for i in ${hosts[0]}; do
#    scp -r deploy/video-recommender td@tuijian${i}:/data/
#done

for i in ${hosts[@]}; do
    echo "tuijian$i"
    scp target/video-recommender-1.0-SNAPSHOT.jar  td@tuijian$i:/data/video-recommender/bin/
    ssh td@tuijian$i "cd /data/video-recommender/bin/; source ~/.bash_profile; ./restart.sh"
    sleep 1
done
