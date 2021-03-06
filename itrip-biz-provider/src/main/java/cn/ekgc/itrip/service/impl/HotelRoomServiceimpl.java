package cn.ekgc.itrip.service.impl;

import cn.ekgc.itrip.dao.HotelRoomDao;
import cn.ekgc.itrip.pojo.vo.HotelRoom;
import cn.ekgc.itrip.pojo.vo.SearchHotelRoomVO;
import cn.ekgc.itrip.pojo.vo.ValidateRoomStoreVO;
import cn.ekgc.itrip.service.HotelRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>爱旅行-酒店房间模块业务层接口实现类</b>
 */
@Service("hotelRoomService")
@Transactional
public class HotelRoomServiceimpl implements HotelRoomService {
	@Autowired
	private HotelRoomDao hotelRoomDao;
	@Autowired
	private HotelOrderDao hotelOrderDao;
	/**
	 * <b>查询酒店列表-此刻可以预定的房间列表</b>
	 * @param searchHotelRoomVO
	 * @return
	 * @throws Exception
	 */

	public List<HotelRoom> queryHotelRoomByHotel(SearchHotelRoomVO searchHotelRoomVO) throws Exception {
		List<HotelRoom> resultList = new ArrayList<HotelRoom>();
		//根据酒店id查询该酒店所有的房间列表
		HotelRoom query = new HotelRoom();
		query.setHotelId(searchHotelRoomVO.getHotelId());
		List<HotelRoom> hotelRoomList = hotelRoomDao.findListByQuery(query);
		//遍历该列表，根据房间id和当前时间查询临时库存数量
		if (hotelRoomList != null && hotelRoomList.size() > 0){
			for (HotelRoom hotelRoom : hotelRoomList){
				//遍历该列表，根据房间id和当前时间查询临时库存数量
				//封装查询参数
				Map<String,Object> queryMap = new HashMap<String,Object>();
				queryMap.put("roomId",hotelRoom.getId());
				//记录时间beginDate
				queryMap.put("beginDate",searchHotelRoomVO.getStartDate());
				Integer store = hotelRoomDao.queryTempStore(queryMap);
				if (store == null){
					//如果临时库存不存在，查询总库存数量
					queryMap.put("productId",hotelRoom.getId());
					store = hotelRoomDao.queryTotalStore(queryMap);
				}
				//计算可用库存，如果库存车大于0
				if (store != null && store > 0 ){
					//查询此时该房间订单表中处于未支付和支付成功的订单数量
					Map<String,Object> orderQueryMap = new HashMap<String,Object>();
					orderQueryMap.put("roomId",hotelRoom.getId());
					orderQueryMap.put("startDate",searchHotelRoomVO.getStartDate());
					orderQueryMap.put("endDate",searchHotelRoomVO.getEndDate());
					Integer orderRoomCount = hotelOrderDao.findOrderRoomCountByQuery(orderQueryMap);
					//使用库存-订单数量，如果大于0则说明房间可用，那么加入最终的结果列表
					if (store - orderRoomCount > 0 ){
						resultList.add(hotelRoom);
					}
				}
			}
		}


		return resultList;
	}

	/**
	 * <b>根据主键查询房间信息</b>
	 * @param roomId
	 * @return
	 * @throws Exception
	 */
	public HotelRoom getHotelRoomById(Long roomId) throws Exception {
		//创建查询对象
		HotelRoom query = new HotelRoom();
		query.setId(roomId);

		//进行列表查询
		List<HotelRoom> hotelRoomList = hotelRoomDao.findListByQuery(query);

		if (hotelRoomList != null && hotelRoomList.size() >0 ){
			return hotelRoomList.get(0);
		}
		return new HotelRoom();
	}

	/**
	 * <b>根据查询获得房间数量</b>
	 * @param validateRoomStoreVO
	 * @return
	 * @throws Exception
	 */
	public int getHotelRoomStoreByDate(ValidateRoomStoreVO validateRoomStoreVO) throws Exception {
		Map<String,Object> queryMap = new HashMap<String,Object>();
        queryMap.put("roomId",validateRoomStoreVO.getRoomId());
        queryMap.put("beignDate",validateRoomStoreVO.getCheckInDate());
        Integer store = hotelRoomDao.queryTempStore(queryMap);

        if (store == null){
        	//如果临时库存不存在，那么就查总库存
	        queryMap.put("productId",validateRoomStoreVO.getRoomId());
	        store = hotelRoomDao.queryTotalStore(queryMap);
        }
        //计算可用库存，如果库存大于0
		if (store != null && store > 0 ){
			//查询此时该房间订单表中未支付和已支付的订单数量
			Map<String,Object> orderQueryMap = new HashMap<String,Object>();
			orderQueryMap.put("roomId",validateRoomStoreVO.getRoomId());
			orderQueryMap.put("startDate",validateRoomStoreVO.getCheckInDate());
			orderQueryMap.put("endDate",validateRoomStoreVO.getCheckOutDate());
			Integer orderRoomCount = hotelOrderDao.findOrderRoomCountByQuery(orderQueryMap);

			if (store - orderRoomCount > 0 ){
				//减去未支付的订单和已支付的订单大于0，那么表示还有房间
				//返回房间数量
				return store - orderRoomCount;
			}

		}
		return 0;
	}
}
